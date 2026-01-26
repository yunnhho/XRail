package com.dev.XRail.domain.schedule.repository;

import com.dev.XRail.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepositoryCustom {
    // 기본 CRUD + QueryDSL Custom 메서드 사용 가능
}