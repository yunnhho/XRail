package com.dev.XRail.domain.reservation.repository;

import com.dev.XRail.domain.reservation.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // [Critical] 구간 중복 체크 쿼리 (Overlap Check)
    // 조건: 같은 스케줄, 같은 좌석인데 구간이 겹치는 티켓이 존재하는가?
    @Query("SELECT count(t) > 0 " +
            "FROM Ticket t " +
            "WHERE t.schedule.id = :scheduleId " +
            "AND t.seat.id = :seatId " +
            "AND t.status != 'CANCELLED' " + // 취소된 표는 제외
            "AND (t.startStationIdx < :reqEndIdx AND t.endStationIdx > :reqStartIdx)")
    boolean existsOverlap(
            @Param("scheduleId") Long scheduleId,
            @Param("seatId") Long seatId,
            @Param("reqStartIdx") Integer reqStartIdx,
            @Param("reqEndIdx") Integer reqEndIdx
    );
}