package com.dev.XRail.domain.train.repository;

import com.dev.XRail.domain.train.entity.Carriage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarriageRepository extends JpaRepository<Carriage, Long> {
}
