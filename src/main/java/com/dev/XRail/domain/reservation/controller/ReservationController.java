package com.dev.XRail.domain.reservation.controller;

import com.dev.XRail.common.dto.ApiResponse;
import com.dev.XRail.domain.reservation.dto.ReservationRequest;
import com.dev.XRail.domain.reservation.service.ReservationService;
import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository; // ID 조회를 위해 주입

    @PostMapping
    public ApiResponse<Long> createReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ReservationRequest request
    ) {
        // 1. JWT 토큰의 Subject(loginId)로 실제 DB 유저 조회
        // (실무에선 ArgumentResolver나 Service 내부에서 처리하지만, 빠른 구현을 위해 여기서 처리)
        Member member = memberRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저 정보를 찾을 수 없습니다."));

        // 2. 예매 서비스 호출 (Kafka 알림까지 자동 수행됨)
        Long reservationId = reservationService.createReservation(member.getId(), request);

        return ApiResponse.success(reservationId);
    }
}