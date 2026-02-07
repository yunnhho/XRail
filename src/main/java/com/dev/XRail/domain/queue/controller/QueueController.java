package com.dev.XRail.domain.queue.controller;

import com.dev.XRail.common.dto.ApiResponse;
import com.dev.XRail.common.exception.BusinessException;
import com.dev.XRail.domain.queue.dto.QueueResponse;
import com.dev.XRail.domain.queue.service.WaitingQueueService;
import com.dev.XRail.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final WaitingQueueService waitingQueueService;
    private final MemberRepository memberRepository;

    @PostMapping("/token")
    public ApiResponse<String> registerQueue(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        waitingQueueService.registerQueue(userId);
        return ApiResponse.success("대기열에 등록되었습니다.");
    }

    @GetMapping("/status")
    public ApiResponse<QueueResponse> getQueueStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        QueueResponse status = waitingQueueService.getQueueStatus(userId);
        return ApiResponse.success(status);
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) throw new BusinessException("UNAUTHORIZED", "로그인 필요");
        return memberRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음"))
                .getId();
    }
}