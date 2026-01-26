package com.dev.XRail.domain.schedule.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import com.dev.XRail.domain.station.entity.Route;
import com.dev.XRail.domain.train.entity.Train;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Table(name = "schedules", indexes = {
        // [Performance] 사용자 검색 패턴 최적화 (날짜 + 노선 + 시간순)
        @Index(name = "idx_schedule_search", columnList = "departure_date, route_id, departure_time")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate; // 파티셔닝 키 (월 단위 분리 권장)

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;

    public Schedule(Route route, Train train, LocalDate departureDate, LocalTime departureTime, LocalTime arrivalTime) {
        this.route = route;
        this.train = train;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}