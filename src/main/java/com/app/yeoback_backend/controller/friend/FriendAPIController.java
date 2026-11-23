package com.app.yeoback_backend.controller.friend;

import com.app.yeoback_backend.auth.CustomUserDetails;
import com.app.yeoback_backend.dto.FriendDTO;
import com.app.yeoback_backend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 친구 API (/api/friends) — 모두 로그인 필요 (Authorization: Bearer). 나는 언제나 로그인 회원
 *  GET    /api/friends                         내 친구 목록
 *  GET    /api/friends/requests?box=received   받은 친구 신청 목록 (box=sent 면 보낸 신청)
 *  GET    /api/friends/requests/count          받은 친구 신청 수 → { count }
 *  GET    /api/friends/search?keyword=         닉네임으로 회원 찾기 (나와의 관계 state 포함, 최대 20명)
 *  GET    /api/friends/{memberId}              이 회원과 나의 관계 → { memberId, state }
 *  POST   /api/friends/{memberId}              친구 신청 (상대가 이미 신청했으면 바로 친구)
 *  POST   /api/friends/{memberId}/accept       받은 신청 수락
 *  DELETE /api/friends/{memberId}              신청 취소 · 거절 · 친구 끊기
 * state: none · requested(내가 신청) · received(나에게 신청) · friend
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendAPIController {
    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<List<FriendDTO>> friends(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(friendService.getFriends(user.getId()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendDTO>> requests(@AuthenticationPrincipal CustomUserDetails user,
                                                    @RequestParam(required = false) String box) {
        return ResponseEntity.ok(friendService.getRequests(user.getId(), box));
    }

    @GetMapping("/requests/count")
    public ResponseEntity<Map<String, Integer>> requestCount(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(friendService.countReceived(user.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FriendDTO>> search(@AuthenticationPrincipal CustomUserDetails user,
                                                  @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(friendService.search(user.getId(), keyword));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<FriendDTO> state(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long memberId) {
        return ResponseEntity.ok(friendService.getState(user.getId(), memberId));
    }

    @PostMapping("/{memberId}")
    public ResponseEntity<FriendDTO> request(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long memberId) {
        return ResponseEntity.ok(friendService.request(user.getId(), memberId));
    }

    @PostMapping("/{memberId}/accept")
    public ResponseEntity<FriendDTO> accept(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long memberId) {
        return ResponseEntity.ok(friendService.accept(user.getId(), memberId));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<FriendDTO> remove(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long memberId) {
        return ResponseEntity.ok(friendService.remove(user.getId(), memberId));
    }
}
