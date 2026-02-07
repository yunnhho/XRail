package com.dev.XRail.domain.reservation.controller;

import com.dev.XRail.common.dto.ApiResponse;
import com.dev.XRail.common.exception.BusinessException;
import com.dev.XRail.domain.reservation.dto.ReservationDetailResponse;
import com.dev.XRail.domain.reservation.dto.ReservationRequest;
import com.dev.XRail.domain.reservation.service.ReservationService;
import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.entity.User;
import com.dev.XRail.domain.user.repository.MemberRepository;
import com.dev.XRail.domain.user.repository.NonMemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final NonMemberRepository nonMemberRepository;

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            log.error("[getUser] UserDetails is null");
            throw new BusinessException("UNAUTHORIZED", "로그인이 필요합니다.");
        }
        String username = userDetails.getUsername();
        log.debug("[getUser] Searching for user with username/accessCode: {}", username);
        
        // 1. 회원 조회
        Optional<Member> member = memberRepository.findByLoginId(username);
        if (member.isPresent()) {
            log.debug("[getUser] Found member: {}", member.get().getId());
            return member.get();
        }
        
        // 2. 비회원 조회 (AccessCode)
        User nonMember = nonMemberRepository.findByAccessCode(username)
                .map(m -> (User) m)
                .orElseThrow(() -> {
                    log.error("[getUser] User not found for accessCode: {}", username);
                    return new BusinessException("USER_NOT_FOUND", "유저 정보를 찾을 수 없습니다.");
                });
        
        log.debug("[getUser] Found non-member: {}", nonMember.getId());
        return nonMember;
    }

    @GetMapping
    public ApiResponse<org.springframework.data.domain.Page<ReservationDetailResponse>> getReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = getUser(userDetails);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("reservedAt").descending());
        return ApiResponse.success(reservationService.getMyReservations(user.getId(), pageable));
    }

    @DeleteMapping("/{reservationId}")
    public ApiResponse<Void> cancelReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        User user = getUser(userDetails);
        reservationService.cancelReservation(user.getId(), reservationId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{reservationId}/pay")
    public ApiResponse<String> payReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        User user = getUser(userDetails);
        String accessCode = reservationService.payReservation(user.getId(), reservationId);
        return ApiResponse.success(accessCode);
    }

    @PostMapping
    public ApiResponse<Long> createReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ReservationRequest request
    ) {
        User user = getUser(userDetails);

        log.info("[Reservation Start] User: {}, Schedule: {}, Seats: {}", user.getId(), request.getScheduleId(), request.getSeatIds());

        Long reservationId = reservationService.createReservation(user.getId(), request);

        return ApiResponse.success(reservationId);
    }
}
