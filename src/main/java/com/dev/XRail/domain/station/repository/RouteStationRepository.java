package com.dev.XRail.domain.station.repository;

import com.dev.XRail.domain.station.entity.RouteStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RouteStationRepository extends JpaRepository<RouteStation, Long> {

    // 특정 노선에서 특정 역의 정보(순서, 거리) 조회
    @Query("SELECT rs FROM RouteStation rs WHERE rs.route.id = :routeId AND rs.station.name = :stationName")
    Optional<RouteStation> findByRouteIdAndStationName(
            @Param("routeId") Long routeId,
            @Param("stationName") String stationName
    );
}