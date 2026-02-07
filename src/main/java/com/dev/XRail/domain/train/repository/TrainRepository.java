package com.dev.XRail.domain.train.repository;

import com.dev.XRail.domain.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {
}
