package com.dev.XRail.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationResponse {
    private Long reservationId;
    private String message;

    public static ReservationResponse success(Long reservationId) {
        return new ReservationResponse(reservationId, "예약이 성공적으로 완료되었습니다.");
    }
}