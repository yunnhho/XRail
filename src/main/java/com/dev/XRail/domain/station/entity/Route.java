package com.dev.XRail.domain.station.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "routes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 노선명 (경부선, 호남선...)

    // 노선에 포함된 역 목록 (순서 중요)
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<RouteStation> routeStations = new ArrayList<>();

    public Route(String name) {
        this.name = name;
    }
}