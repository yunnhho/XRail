package com.dev.XRail.domain.station.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Station extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // 역 이름 (서울, 부산, 동대구...)

    public Station(String name) {
        this.name = name;
    }
}