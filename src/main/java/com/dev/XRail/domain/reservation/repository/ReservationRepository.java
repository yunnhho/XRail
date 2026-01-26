package com.dev.XRail.domain.reservation.repository;

import com.dev.XRail.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 유저의 예매 내역 조회 (최신순)
    List<Reservation> findByUserIdOrderByReservedAtDesc(Long userId);
}