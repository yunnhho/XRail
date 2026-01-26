package com.dev.XRail.domain.train.repository;

import com.dev.XRail.domain.train.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    // 필요 시 특정 열차/호차의 좌석 조회 메서드 추가 가능
}