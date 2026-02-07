package com.dev.XRail.domain.reservation.repository;

import com.dev.XRail.domain.reservation.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT count(t) > 0 FROM Ticket t " +
            "WHERE t.schedule.id = :scheduleId " +
            "AND t.seat.id = :seatId " +
            "AND t.reservation.status != 'CANCELLED' " +
            "AND (t.startStationIdx < :reqEndIdx AND t.endStationIdx > :reqStartIdx)")
    boolean existsOverlap(
            @Param("scheduleId") Long scheduleId,
            @Param("seatId") Long seatId,
            @Param("reqStartIdx") Integer reqStartIdx,
            @Param("reqEndIdx") Integer reqEndIdx
    );

    // 특정 스케줄의 특정 구간에 이미 팔린 티켓 목록 조회
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.seat s " +
            "JOIN FETCH s.carriage " +
            "WHERE t.schedule.id = :scheduleId " +
            "AND t.reservation.status != 'CANCELLED' " +
            "AND (t.startStationIdx < :reqEndIdx AND t.endStationIdx > :reqStartIdx)")
    List<Ticket> findOverlappingTickets(
            @Param("scheduleId") Long scheduleId,
            @Param("reqStartIdx") Integer reqStartIdx,
            @Param("reqEndIdx") Integer reqEndIdx
    );

    // [Reconciliation] 특정 스케줄+좌석의 유효한(RESERVED) 티켓 목록 조회
    @Query("SELECT t FROM Ticket t " +
            "WHERE t.schedule.id = :scheduleId " +
            "AND t.seat.id = :seatId " +
            "AND t.status = 'RESERVED'") // 취소된 표는 비트마스크에서 빠져야 함
    List<Ticket> findAllActiveTickets(@Param("scheduleId") Long scheduleId,
                                      @Param("seatId") Long seatId);

    @Query("SELECT t FROM Ticket t " +
            "WHERE t.schedule.id = :scheduleId " +
            "AND t.status = 'RESERVED'")
    List<Ticket> findAllActiveTicketsByScheduleId(@Param("scheduleId") Long scheduleId);

    // [Sold Out Check] 특정 스케줄의 특정 구간에 이미 팔린 티켓 수 조회 (성능 최적화: Entity 로딩 없이 개수만)
    @Query("SELECT COUNT(t) FROM Ticket t " +
            "WHERE t.schedule.id = :scheduleId " +
            "AND t.reservation.status != 'CANCELLED' " +
            "AND (t.startStationIdx < :reqEndIdx AND t.endStationIdx > :reqStartIdx)")
    long countOverlappingTickets(
            @Param("scheduleId") Long scheduleId,
            @Param("reqStartIdx") Integer reqStartIdx,
            @Param("reqEndIdx") Integer reqEndIdx
    );

    // [Admin] 티켓 조회 (필터링 + 페이징)
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.reservation r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH t.schedule s " +
            "JOIN FETCH s.train tr " +
            "JOIN FETCH s.route ro " +
            "JOIN FETCH t.startStation ss " +
            "JOIN FETCH t.endStation es " +
            "JOIN FETCH t.seat st " +
            "JOIN FETCH st.carriage c " +
            "WHERE (:date IS NULL OR s.departureDate = :date) " +
            "AND (:routeId IS NULL OR ro.id = :routeId)")
    org.springframework.data.domain.Page<Ticket> findTicketsWithFilter(
            @Param("date") java.time.LocalDate date,
            @Param("routeId") Long routeId,
            org.springframework.data.domain.Pageable pageable
    );

    // [Admin] 통계용: 특정 날짜의 총 매출 및 티켓 수
    @Query("SELECT new com.dev.XRail.common.dto.DailyStatsDto(SUM(t.price), COUNT(t)) " +
            "FROM Ticket t WHERE t.schedule.departureDate = :date AND t.reservation.status = 'PAID'")
    com.dev.XRail.common.dto.DailyStatsDto getDailyStats(@Param("date") java.time.LocalDate date);
}