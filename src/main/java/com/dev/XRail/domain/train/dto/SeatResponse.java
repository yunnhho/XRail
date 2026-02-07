package com.dev.XRail.domain.train.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatResponse {
    private Long seatId;
    private Integer carriageNumber; // 호차 번호 (1호차)
    private String seatNumber;      // 좌석 번호 (1A)
    
    @JsonProperty("isReserved")
    private boolean isReserved;     // 예약 여부 (true=예약불가, false=예약가능)
}