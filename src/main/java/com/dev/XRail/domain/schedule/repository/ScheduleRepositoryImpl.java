package com.dev.XRail.domain.schedule.repository;

import com.dev.XRail.domain.schedule.entity.QSchedule;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.dev.XRail.domain.schedule.entity.QSchedule.schedule;
import static com.dev.XRail.domain.station.entity.QRoute.route;
import static com.dev.XRail.domain.train.entity.QTrain.train;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Schedule> findSchedules(Long routeId, LocalDate date, LocalTime startTime) {
        return queryFactory
                .selectFrom(schedule)
                .join(schedule.route, route).fetchJoin() // N+1 방지 Fetch Join
                .join(schedule.train, train).fetchJoin()
                .where(
                        schedule.route.id.eq(routeId),
                        schedule.departureDate.eq(date),
                        schedule.departureTime.goe(startTime) // 출발 시간 이후인 것만
                )
                .orderBy(schedule.departureTime.asc())
                .fetch();
    }
}