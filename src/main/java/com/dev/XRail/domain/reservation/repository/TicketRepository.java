package com.dev.XRail.domain.reservation.repository;

import com.dev.XRail.domain.reservation.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // 구간 중복 체크 쿼리
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

    // 특정 스케줄의 특정 구간에 이미 팔린 티켓 목록 조회
    @Query("SELECT t FROM Ticket t " +
            "WHERE t.schedule.id = :scheduleId " +
            "AND t.status != 'CANCELLED' " +
            "AND (t.startStationIdx < :reqEndIdx AND t.endStationIdx > :reqStartIdx)")
    List<Ticket> findOverlappingTickets(
            @Param("scheduleId") Long scheduleId,
            @Param("reqStartIdx") Integer reqStartIdx,
            @Param("reqEndIdx") Integer reqEndIdx
    );
}