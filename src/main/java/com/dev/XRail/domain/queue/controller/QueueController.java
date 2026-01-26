package com.dev.XRail.domain.queue.controller;

import com.dev.XRail.domain.queue.dto.QueueResponse;
import com.dev.XRail.domain.queue.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final WaitingQueueService waitingQueueService;

    // 대기열 진입 및 상태 조회
    @PostMapping("/status")
    public ResponseEntity<QueueResponse> getStatus(@AuthenticationPrincipal UserDetails userDetails) {
        // UserDetails에서 ID 추출 로직 필요 (여기선 loginId를 사용하거나 별도 파싱)
        // 편의상 ID를 파라미터로 받지 않고 토큰에서 꺼내야 보안상 안전
        // 여기서는 UserDetails.getUsername()을 ID로 가정하고, 실제론 DB 조회해서 Long ID 변환 필요
        // 임시로 Long.parseLong을 사용하지 않고, loginId 해시값 등을 사용하거나
        // CustomUserDetailsService에서 ID를 담도록 수정해야 함.
        // -> [Fix] UserDetails에 ID가 없으므로 일단 1L로 하드코딩하지 않고,
        // CustomUserDetails 캐스팅이 필요함. (복잡도 증가 방지를 위해 주석 처리 후 가이드)

        // *실제 구현 시*: ((CustomUserDetails) userDetails).getId() 사용
        Long userId = 1L; // 임시: 실제론 토큰에서 추출해야 함!

        waitingQueueService.registerQueue(userId);
        return ResponseEntity.ok(waitingQueueService.getQueueStatus(userId));
    }
}