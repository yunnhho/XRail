package com.dev.XRail.domain.schedule.dto;

import com.dev.XRail.domain.schedule.entity.Schedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ScheduleResponse {
    private Long scheduleId;
    private String trainType;   // KTX, 무궁화
    private String trainNumber; // 101, 205
    private String routeName;   // 경부선
    private LocalDate departureDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Long price; // 운임 요금
    
    @com.fasterxml.jackson.annotation.JsonProperty("isSoldOut")
    private boolean isSoldOut; // 매진 여부

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .trainType(schedule.getTrain().getTrainType().name())
                .trainNumber(schedule.getTrain().getTrainNumber())
                .routeName(schedule.getRoute().getName())
                .departureDate(schedule.getDepartureDate())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .price(0L) // 기본값, 서비스에서 계산 후 주입
                .isSoldOut(false) // 기본값
                .build();
    }
    
    public void setPrice(Long price) {
        this.price = price;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setSoldOut(boolean soldOut) {
        isSoldOut = soldOut;
    }
}