package com.dev.XRail.domain.reservation.dto;

import com.dev.XRail.domain.reservation.entity.Reservation;
import com.dev.XRail.domain.reservation.entity.ReservationStatus;
import com.dev.XRail.domain.reservation.entity.Ticket;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReservationDetailResponse {
    private Long reservationId;
    private ReservationStatus status;
    private Long totalPrice;
    private LocalDateTime reservedAt;
    private List<TicketDetail> tickets;

    @Getter
    @Builder
    public static class TicketDetail {
        private Long ticketId;
        private String trainNumber;
        private Integer carriageNumber; // 호차 번호 추가
        private String seatNumber;
        private String startStation;
        private String endStation;
        private String departureTime;
        private String arrivalTime;
        private String departureDate;

        public static TicketDetail from(Ticket ticket) {
            return TicketDetail.builder()
                    .ticketId(ticket.getId())
                    .trainNumber(ticket.getSchedule().getTrain().getTrainNumber())
                    .carriageNumber(ticket.getSeat().getCarriage().getCarriageNumber()) // 호차 번호 매핑
                    .seatNumber(ticket.getSeat().getSeatNumber())
                    .startStation(ticket.getStartStation().getName())
                    .endStation(ticket.getEndStation().getName())
                    .departureTime(ticket.getSchedule().getDepartureTime().toString())
                    .arrivalTime(ticket.getSchedule().getArrivalTime().toString())
                    .departureDate(ticket.getSchedule().getDepartureDate().toString())
                    .build();
        }
    }

    public static ReservationDetailResponse from(Reservation reservation) {
        return ReservationDetailResponse.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .reservedAt(reservation.getReservedAt())
                .tickets(reservation.getTickets().stream().map(TicketDetail::from).collect(Collectors.toList()))
                .build();
    }
}