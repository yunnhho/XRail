package com.dev.XRail.domain.reservation.repository;

import com.dev.XRail.domain.reservation.entity.Reservation;
import com.dev.XRail.domain.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.tickets t JOIN FETCH t.schedule s JOIN FETCH s.train tr JOIN FETCH t.seat st WHERE r.user.id = :userId ORDER BY r.reservedAt DESC")
    List<Reservation> findAllByUserIdWithDetails(@Param("userId") Long userId);

    // [Pagination Support] Batch Fetch를 활용하므로 Fetch Join 제거
    org.springframework.data.domain.Page<Reservation> findAllByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, java.time.LocalDateTime dateTime);
}
