package com.dev.XRail.domain.station.repository;

import com.dev.XRail.domain.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    // 역 이름으로 조회 (예: "서울", "부산")
    Optional<Station> findByName(String name);
}