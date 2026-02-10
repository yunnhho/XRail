package com.dev.XRail.common.interceptor;

import com.dev.XRail.common.exception.BusinessException;
import com.dev.XRail.domain.queue.service.WaitingQueueService;
import com.dev.XRail.domain.user.entity.User;
import com.dev.XRail.domain.user.repository.MemberRepository;
import com.dev.XRail.domain.user.repository.NonMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class QueueInterceptor implements HandlerInterceptor {

    private final WaitingQueueService waitingQueueService;
    private final MemberRepository memberRepository;
    private final NonMemberRepository nonMemberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        // [Test Mode] 부하 테스트용 백도어

        String testUserId = request.getHeader("X-User-Id");

        if (testUserId != null) {
            Long userId = Long.valueOf(testUserId);
            if (!waitingQueueService.isAllowedByToken(userId)) {
                throw new BusinessException("ACCESS_DENIED", "대기열 통과 못함");
            }
            return true;
        }

        // 1. 인증 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
            return true;
        }
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String username = userDetails.getUsername();

        // 2. 유저 ID(PK) 조회 (회원 or 비회원)
        Long userId = memberRepository.findByLoginId(username)
                .map(User::getId)
                .orElseGet(() -> nonMemberRepository.findByAccessCode(username)
                        .map(User::getId)
                        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "유저 정보를 찾을 수 없습니다."))
                );

        // 3. 대기열 토큰(Active) 확인
        if (!waitingQueueService.isAllowedByToken(userId)) {
            throw new BusinessException("ACCESS_DENIED", "접속 대기 순서가 아닙니다. 대기열을 통해 진입해주세요.");
        }
        return true;
    }
}
