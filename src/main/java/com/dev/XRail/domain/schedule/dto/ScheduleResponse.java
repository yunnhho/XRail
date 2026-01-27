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

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .trainType(schedule.getTrain().getTrainType().name())
                .trainNumber(schedule.getTrain().getTrainNumber())
                .routeName(schedule.getRoute().getName())
                .departureDate(schedule.getDepartureDate())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .build();
    }
}