package com.dev.XRail.domain.station.repository;

import com.dev.XRail.domain.station.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
