package com.dev.XRail.domain.reservation.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.station.entity.Station;
import com.dev.XRail.domain.train.entity.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tickets", indexes = {
        // [Logic] 특정 스케줄의 특정 좌석이 '어느 구간'에 팔렸는지 조회용
        @Index(name = "idx_ticket_segment", columnList = "schedule_id, seat_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    // [Segment Booking Core] 구간 시작 역
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_station_id", nullable = false)
    private Station startStation;

    // [Segment Booking Core] 구간 도착 역
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_station_id", nullable = false)
    private Station endStation;

    // [Bitmasking Index] 비트 연산용 순서 인덱스 (0, 1, 2...)
    // 예: 서울(0)->대전(1) 예매 시 start=0, end=1. 점유 비트: 0번 비트.
    @Column(name = "start_station_idx", nullable = false)
    private Integer startStationIdx;

    @Column(name = "end_station_idx", nullable = false)
    private Integer endStationIdx;

    @Column(nullable = false)
    private Long price; // 개별 티켓 가격

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    @Builder
    public Ticket(Reservation reservation, Schedule schedule, Seat seat,
                  Station startStation, Station endStation,
                  Integer startStationIdx, Integer endStationIdx, Long price) {
        this.reservation = reservation;
        this.schedule = schedule;
        this.seat = seat;
        this.startStation = startStation;
        this.endStation = endStation;
        this.startStationIdx = startStationIdx;
        this.endStationIdx = endStationIdx;
        this.price = price;
        this.status = TicketStatus.RESERVED;
    }

    public enum TicketStatus {
        RESERVED,  // 예약됨 (유효)
        CANCELLED, // 취소됨 (무효)
        USED       // 사용됨
    }
}