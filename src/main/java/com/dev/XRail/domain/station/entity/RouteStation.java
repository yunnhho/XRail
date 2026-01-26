package com.dev.XRail.domain.station.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "route_stations", indexes = {
        @Index(name = "idx_route_station_seq", columnList = "route_id, station_sequence") // 순서대로 조회용
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteStation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_station_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "station_sequence", nullable = false)
    private Integer stationSequence; // 정차 순서 (0, 1, 2...) -> Bitmask Index로 사용됨

    @Column(name = "cumulative_distance", nullable = false)
    private Double cumulativeDistance; // 기점으로부터의 거리 (km) - 요금 계산용

    public RouteStation(Route route, Station station, Integer stationSequence, Double cumulativeDistance) {
        this.route = route;
        this.station = station;
        this.stationSequence = stationSequence;
        this.cumulativeDistance = cumulativeDistance;
    }
}