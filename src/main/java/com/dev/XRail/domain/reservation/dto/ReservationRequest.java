package com.dev.XRail.domain.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReservationRequest {
    private Long scheduleId;
    private Long seatId;
    private Long startStationId;
    private Long endStationId;

    // 프론트엔드 혹은 역 정보 조회 후 계산된 인덱스
    private Integer startStationIdx;
    private Integer endStationIdx;

    private Long price;
}