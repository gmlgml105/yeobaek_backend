package com.app.yeoback_backend.controller.profile;

import com.app.yeoback_backend.auth.CustomUserDetails;
import com.app.yeoback_backend.dto.ProfileDTO;
import com.app.yeoback_backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 API (/api/profile) — 모두 로그인 필요 (Authorization: Bearer)
 *  GET    /api/profile/{memberId} 프로필 조회 — 본인이면 내 프로필(세부 설정 포함), 타인이면 공개용 회원 프로필
 *  PUT    /api/profile           닉네임 · 소개 수정
 *  PATCH  /api/profile/settings  취향 · 키워드 · 공개 설정 · 컬러 수정 (보낸 항목만)
 *  POST   /api/profile/avatar    프로필 사진 올리기 (multipart, 필드 이름 file)
 *  DELETE /api/profile/avatar    프로필 사진 지우기
 * 수정 API 응답은 모두 수정 후의 내 프로필 전체(ProfileDTO). 수정은 언제나 로그인한 본인 것만 바뀐다
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileAPIController {
    private final ProfileService profileService;

    @GetMapping("/{memberId}")
    public ResponseEntity<ProfileDTO> getProfile(@AuthenticationPrincipal CustomUserDetails user,
                                                 @PathVariable Long memberId) {
        return ResponseEntity.ok(profileService.getProfile(user.getId(), memberId));
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(@AuthenticationPrincipal CustomUserDetails user,
                                                    @RequestBody ProfileDTO request) {
        return ResponseEntity.ok(profileService.updateProfile(user.getId(), request));
    }

    @PatchMapping("/settings")
    public ResponseEntity<ProfileDTO> updateSettings(@AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestBody ProfileDTO request) {
        return ResponseEntity.ok(profileService.updateSettings(user.getId(), request));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ProfileDTO> uploadAvatar(@AuthenticationPrincipal CustomUserDetails user,
                                                   @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadAvatar(user.getId(), file));
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<ProfileDTO> deleteAvatar(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(profileService.deleteAvatar(user.getId()));
    }
}
