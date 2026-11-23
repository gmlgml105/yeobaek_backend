package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.exception.ApiException;
import com.app.yeoback_backend.common.enumeration.Status;
import com.app.yeoback_backend.common.enumeration.Visibility;
import com.app.yeoback_backend.common.exception.MemberNotFoundException;
import com.app.yeoback_backend.dto.FileDTO;
import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.dto.MemberSettingDTO;
import com.app.yeoback_backend.dto.ProfileDTO;
import com.app.yeoback_backend.mapper.FileMapper;
import com.app.yeoback_backend.mapper.MemberSettingMapper;
import com.app.yeoback_backend.repository.MemberDAO;
import com.app.yeoback_backend.repository.MemberRelDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 개인 프로필 · 세부 설정 조회와 수정
 *  - 조회: tbl_member(닉네임 · 소개) + tbl_member_setting + 취향 · 키워드 + 프로필 사진
 *  - 설정 행이나 닉네임이 없는 회원(이 기능 이전 가입자)은 조회할 때 기본값으로 채운다
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private static final int ALIAS_MIN = 2;
    private static final int ALIAS_MAX = 12;
    private static final int BIO_MAX = 80;
    private static final int CHOICE_MAX = 30;
    // 프론트 lib/accent.ts 의 ACCENT_PALETTES id, 또는 직접 고른 #rrggbb
    private static final Pattern THEME_COLOR = Pattern.compile("^(sky|aqua|blossom|lavender|mint|#[0-9a-f]{6})$");

    private final MemberDAO memberDAO;
    private final MemberSettingMapper settingMapper;
    private final FileMapper fileMapper;
    private final FileStorageService fileStorageService;
    private final MemberDefaultsService memberDefaultsService;
    private final MemberRelDAO memberRelDAO;

    @Transactional
    public ProfileDTO getProfile(Long memberId) {
        MemberDTO member = member(memberId);

        // 이 기능 이전에 가입한 회원 보정
        if (member.getMemberAlias() == null || member.getMemberAlias().isBlank()) {
            member.setMemberAlias(memberDefaultsService.newNickname());
            memberDAO.updateProfile(memberId, member.getMemberAlias(), member.getMemberBio());
        }
        MemberSettingDTO setting = settingMapper.selectByMemberId(memberId).orElseGet(() -> {
            memberDefaultsService.createDefaultSetting(memberId);
            return settingMapper.selectByMemberId(memberId).orElseThrow();
        });

        String avatarUrl = fileMapper.selectMemberFile(memberId).map(f -> "/api/files/" + f.getId()).orElse(null);

        ProfileDTO profile = new ProfileDTO();
        profile.setId(member.getId());
        profile.setName(member.getMemberName());
        profile.setEmail(member.getMemberEmail());
        profile.setPhone(member.getMemberPhone());
        profile.setAvatarUrl(avatarUrl);
        profile.setAlias(member.getMemberAlias());
        profile.setBio(member.getMemberBio());
        profile.setPreferences(settingMapper.selectPreferenceNames(memberId));
        profile.setVibes(settingMapper.selectVibeNames(memberId));
        profile.setNameVisibility(orPrivate(setting.getNameVisibility()));
        profile.setPhoneVisibility(orPrivate(setting.getPhoneVisibility()));
        profile.setNoteAutoSave(Boolean.TRUE.equals(setting.getNoteAutoSave()));
        profile.setThemeColor(setting.getThemaColor());
        return profile;
    }

    /**
     * GET /api/profile/{memberId}: 보는 사람(viewerId)이 본인이면 내 프로필, 아니면 공개용 회원 프로필
     */
    @Transactional
    public ProfileDTO getProfile(Long viewerId, Long memberId) {
        return viewerId.equals(memberId) ? getProfile(memberId) : getMemberProfile(viewerId, memberId);
    }

    /**
     * 다른 회원이 보는 프로필 (조회만 하고 아무것도 저장하지 않는다)
     *  - 없는 회원 · 활동 중이 아닌 회원(정지 · 탈퇴)은 404
     *  - 실명 · 연락처는 보는 사람에게 공개된 경우에만 (공개, 또는 '친구만'이고 친구 사이)
     *    공개되지 않은 항목은 값을 비우고 visibility 를 private 로 보낸다
     *  - 이메일과 개인 설정 값은 보내지 않는다
     *  - 설정 행이 없는 회원은 기본값(비공개)으로 본다
     */
    @Transactional(readOnly = true)
    public ProfileDTO getMemberProfile(Long viewerId, Long memberId) {
        MemberDTO member = memberDAO.findById(memberId)
                .filter(m -> m.getMemberStatus() == Status.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "회원을 찾지 못했어요."));
        MemberSettingDTO setting = settingMapper.selectByMemberId(memberId).orElseGet(MemberSettingDTO::new);
        boolean friends = memberRelDAO.areFriends(viewerId, memberId);
        Visibility name = visibleTo(setting.getNameVisibility(), friends);
        Visibility phone = visibleTo(setting.getPhoneVisibility(), friends);

        ProfileDTO profile = new ProfileDTO();
        profile.setId(member.getId());
        profile.setAlias(member.getMemberAlias());
        profile.setBio(member.getMemberBio());
        profile.setAvatarUrl(fileMapper.selectMemberFile(memberId).map(f -> "/api/files/" + f.getId()).orElse(null));
        profile.setPreferences(settingMapper.selectPreferenceNames(memberId));
        profile.setVibes(settingMapper.selectVibeNames(memberId));
        profile.setNameVisibility(name);
        profile.setPhoneVisibility(phone);
        profile.setName(name == Visibility.PRIVATE ? null : member.getMemberName());
        profile.setPhone(phone == Visibility.PRIVATE ? null : member.getMemberPhone());
        return profile;
    }

    private static Visibility orPrivate(Visibility v) {
        return v == null ? Visibility.PRIVATE : v;
    }

    /** 보는 사람에게 보이는 공개 범위: 보이면 원래 값(public · friends), 안 보이면 private */
    private static Visibility visibleTo(Visibility v, boolean friends) {
        Visibility scope = orPrivate(v);
        if (scope == Visibility.PUBLIC || (scope == Visibility.FRIENDS && friends)) return scope;
        return Visibility.PRIVATE;
    }

    /** 프로필 수정 화면: 요청에서 닉네임 · 소개만 읽는다 */
    @Transactional
    public ProfileDTO updateProfile(Long memberId, ProfileDTO request) {
        String alias = request.getAlias() == null ? "" : request.getAlias().trim();
        if (alias.length() < ALIAS_MIN || alias.length() > ALIAS_MAX) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "닉네임은 2~12자로 적어 주세요.");
        }
        String bio = request.getBio() == null ? "" : request.getBio().trim();
        if (bio.length() > BIO_MAX) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "소개는 80자 이내로 적어 주세요.");
        }
        member(memberId);
        memberDAO.updateProfile(memberId, alias, bio);
        return getProfile(memberId);
    }

    /** 세부 설정 화면: 요청에서 설정 항목만 읽고, 보낸 항목만 바꾼다 */
    @Transactional
    public ProfileDTO updateSettings(Long memberId, ProfileDTO request) {
        getProfile(memberId); // 회원 확인 + 설정 행 보정

        MemberSettingDTO setting = new MemberSettingDTO();
        setting.setId(memberId);
        setting.setNameVisibility(request.getNameVisibility());
        setting.setPhoneVisibility(request.getPhoneVisibility());
        setting.setNoteAutoSave(request.getNoteAutoSave());
        if (request.getThemeColor() != null) {
            String color = request.getThemeColor().trim().toLowerCase();
            if (!THEME_COLOR.matcher(color).matches()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "알 수 없는 컬러 조합이에요.");
            }
            setting.setThemaColor(color);
        }
        settingMapper.update(setting);

        if (request.getPreferences() != null) {
            replaceChoices(memberId, request.getPreferences(), "여행 취향",
                    settingMapper::selectPreferenceIdsByNames,
                    settingMapper::deletePreferences,
                    ids -> settingMapper.insertPreferences(memberId, ids));
        }
        if (request.getVibes() != null) {
            replaceChoices(memberId, request.getVibes(), "여행 키워드",
                    settingMapper::selectVibeIdsByNames,
                    settingMapper::deleteVibes,
                    ids -> settingMapper.insertVibes(memberId, ids));
        }
        return getProfile(memberId);
    }

    /** 프로필 사진 올리기: 디스크 저장 → tbl_file · tbl_file_member → 이전 사진 삭제 */
    @Transactional
    public ProfileDTO uploadAvatar(Long memberId, MultipartFile file) {
        member(memberId);
        Optional<FileDTO> previous = fileMapper.selectMemberFile(memberId);

        FileDTO saved = fileStorageService.saveImage(file);
        try {
            fileMapper.insert(saved);
            fileMapper.insertMemberFile(saved.getId(), memberId);
        } catch (RuntimeException e) {
            fileStorageService.delete(saved); // DB 저장에 실패하면 디스크에 남기지 않는다
            throw e;
        }
        previous.ifPresent(this::removeMemberFile);
        return getProfile(memberId);
    }

    /** 프로필 사진 지우기 (닉네임 첫 글자로 돌아감) */
    @Transactional
    public ProfileDTO deleteAvatar(Long memberId) {
        member(memberId);
        fileMapper.selectMemberFile(memberId).ifPresent(this::removeMemberFile);
        return getProfile(memberId);
    }

    private void removeMemberFile(FileDTO file) {
        fileMapper.deleteMemberFile(file.getId());
        fileMapper.deleteById(file.getId());
        fileStorageService.delete(file);
    }

    private void replaceChoices(Long memberId, List<String> names, String label,
                                Function<List<String>, List<Long>> findIds,
                                java.util.function.Consumer<Long> deleteAll,
                                java.util.function.Consumer<List<Long>> insertAll) {
        List<String> unique = new ArrayList<>(new LinkedHashSet<>(names.stream()
                .filter(n -> n != null && !n.isBlank()).map(String::trim).toList()));
        if (unique.size() > CHOICE_MAX) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", label + "은(는) " + CHOICE_MAX + "개까지 고를 수 있어요.");
        }
        List<Long> ids = unique.isEmpty() ? List.of() : findIds.apply(unique);
        if (ids.size() != unique.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "알 수 없는 " + label + "이 있어요.");
        }
        deleteAll.accept(memberId);
        if (!ids.isEmpty()) insertAll.accept(ids);
    }

    private MemberDTO member(Long memberId) {
        return memberDAO.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }
}
