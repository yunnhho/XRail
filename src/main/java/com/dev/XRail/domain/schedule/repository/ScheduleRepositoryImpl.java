package com.dev.XRail.domain.schedule.repository;

import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.station.entity.QRouteStation;
import com.querydsl.jpa.JPAExpressions;
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
    public List<Schedule> findSchedules(Long departureStationId, Long arrivalStationId, LocalDate date, LocalTime startTime) {
        QRouteStation rs1 = new QRouteStation("rs1");
        QRouteStation rs2 = new QRouteStation("rs2");

        return queryFactory
                .selectFrom(schedule)
                .join(schedule.route, route).fetchJoin()
                .join(schedule.train, train).fetchJoin()
                .where(
                        // 1. 선택한 날짜와 시간 조건
                        schedule.departureDate.eq(date),
                        schedule.departureTime.goe(startTime),
                        
                        // 2. 출발역과 도착역이 같은 노선상에 있는지 확인
                        schedule.route.id.in(
                            JPAExpressions
                                .select(rs1.route.id)
                                .from(rs1)
                                .join(rs2).on(rs1.route.id.eq(rs2.route.id))
                                .where(
                                    rs1.station.id.eq(departureStationId),
                                    rs2.station.id.eq(arrivalStationId),
                                    rs1.stationSequence.lt(rs2.stationSequence) // 출발역이 도착역보다 앞서야 함
                                )
                        )
                )
                .orderBy(schedule.departureTime.asc())
                .fetch();
    }
}
