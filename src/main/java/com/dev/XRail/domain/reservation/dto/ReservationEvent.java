package com.dev.XRail.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEvent {
    private Long reservationId;
    private Long userId;
    private Long scheduleId;
    private Long seatId;
    private String trainNumber; // 알림톡 내용용
    private String departureDate;
    private String seatNumber;
    private LocalDateTime eventCreatedAt;
}