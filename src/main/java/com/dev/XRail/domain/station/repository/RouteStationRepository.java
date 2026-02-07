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
    
    // 추가: ID 기반 조회
    @Query("SELECT rs FROM RouteStation rs WHERE rs.route.id = :routeId AND rs.station.id = :stationId")
    Optional<RouteStation> findByRouteIdAndStationId(
            @Param("routeId") Long routeId,
            @Param("stationId") Long stationId
    );

    // 노선 ID와 역 ID로 순서(Index) 조회
    @Query("SELECT rs.stationSequence FROM RouteStation rs " +
            "WHERE rs.route.id = :routeId AND rs.station.id = :stationId")
    Optional<Integer> findSequenceByRouteIdAndStationId(
            @Param("routeId") Long routeId,
            @Param("stationId") Long stationId
    );

    @Query("SELECT rs FROM RouteStation rs WHERE rs.route.id = :routeId ORDER BY rs.stationSequence ASC")
    java.util.List<RouteStation> findByRouteIdOrderByStationSequenceAsc(@Param("routeId") Long routeId);
}