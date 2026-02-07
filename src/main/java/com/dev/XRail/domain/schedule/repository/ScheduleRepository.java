package com.dev.XRail.domain.schedule.repository;

import com.dev.XRail.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepositoryCustom {
    // 기본 CRUD + QueryDSL Custom 메서드 사용 가능
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.train t " +
            "JOIN FETCH s.route r " +
            "WHERE s.id = :id")
    Optional<Schedule> findWithTrainById(@Param("id") Long id);

    @Query("SELECT s.id FROM Schedule s WHERE s.departureDate >= :date")
    List<Long> findActiveScheduleIds(@Param("date") java.time.LocalDate date);

    boolean existsByDepartureDate(java.time.LocalDate departureDate);

    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.train t " +
            "JOIN FETCH s.route r " +
            "WHERE (:date IS NULL OR s.departureDate = :date) " +
            "AND (:routeId IS NULL OR s.route.id = :routeId)")
    org.springframework.data.domain.Page<Schedule> findSchedulesWithFilter(
            @Param("date") java.time.LocalDate date, 
            @Param("routeId") Long routeId, 
            org.springframework.data.domain.Pageable pageable
    );
}