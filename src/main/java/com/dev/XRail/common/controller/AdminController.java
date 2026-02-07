package com.dev.XRail.common.controller;

import com.dev.XRail.common.DataInitializer;
import com.dev.XRail.common.dto.DailyStatsDto;
import com.dev.XRail.domain.reservation.entity.Ticket;
import com.dev.XRail.domain.reservation.repository.TicketRepository;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.entity.NonMember;
import com.dev.XRail.domain.user.entity.User;
import com.dev.XRail.domain.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DataInitializer dataInitializer;
    private final ScheduleRepository scheduleRepository;
    private final TicketRepository ticketRepository;

    @PostMapping("/reset-data")
    public String resetData() {
        dataInitializer.run(); // 강제 실행
        return "데이터가 초기화되었습니다. 기존 예약과 회원 정보가 모두 삭제되고, 열차 데이터가 재생성되었습니다.";
    }

    @GetMapping("/stats")
    public DailyStatsDto getStats() {
        DailyStatsDto stats = ticketRepository.getDailyStats(LocalDate.now());
        if (stats == null || stats.getTotalRevenue() == null) {
            return new DailyStatsDto(0L, 0L);
        }
        return stats;
    }

    @GetMapping("/schedules")
    public Page<AdminScheduleDto> getSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long routeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureDate,desc") String[] sort
    ) {
        String sortField = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return scheduleRepository.findSchedulesWithFilter(date, routeId, pageable)
                .map(AdminScheduleDto::from);
    }

    @GetMapping("/tickets")
    public Page<AdminTicketDto> getTickets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long routeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort
    ) {
        String sortField = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        String entityField = "id";
        if (sortField.equals("reservationId")) entityField = "reservation.id";
        else if (sortField.equals("status")) entityField = "reservation.status";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, entityField));
        return ticketRepository.findTicketsWithFilter(date, routeId, pageable)
                .map(AdminTicketDto::from);
    }

    @Getter
    @Builder
    public static class AdminScheduleDto {
        private Long id;
        private String routeName;
        private String trainInfo;
        private String dateTime;
        private String arrivalTime;

        public static AdminScheduleDto from(Schedule s) {
            return AdminScheduleDto.builder()
                    .id(s.getId())
                    .routeName(s.getRoute().getName())
                    .trainInfo(s.getTrain().getTrainType() + " " + s.getTrain().getTrainNumber())
                    .dateTime(s.getDepartureDate() + " " + s.getDepartureTime())
                    .arrivalTime(s.getArrivalTime().toString())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class AdminTicketDto {
        private Long ticketId;
        private Long reservationId;
        private String userType;
        private String userName;
        private String userDetail;
        private String routeInfo;
        private String seatInfo;
        private String trainInfo;
        private String status;

        public static AdminTicketDto from(Ticket t) {
            User user = t.getReservation().getUser();
            String userType = (user.getRole() != null) ? user.getRole().name() : "UNKNOWN";
            String userName = "Unknown";
            String userDetail = "정보 없음";

            // 프록시 해제 후 실제 객체 확보
            Object unproxiedUser = Hibernate.unproxy(user);

            if (unproxiedUser instanceof Member m) {
                userName = m.getName();
                userDetail = String.format("ID: %s\nEmail: %s\nPhone: %s\nBirth: %s", 
                        m.getLoginId(), m.getEmail(), m.getPhone(), m.getBirthDate());
            } else if (unproxiedUser instanceof NonMember nm) {
                userName = nm.getName();
                userDetail = String.format("Type: Non-Member (Guest)\nName: %s\nPhone: %s\nAccessCode: %s", 
                        nm.getName(), nm.getPhone(), nm.getAccessCode());
            } else {
                userName = "User#" + user.getId();
                userDetail = "Raw User Type: " + user.getClass().getSimpleName();
            }

            return AdminTicketDto.builder()
                    .ticketId(t.getId())
                    .reservationId(t.getReservation().getId())
                    .userType(userType)
                    .userName(userName)
                    .userDetail(userDetail)
                    .routeInfo(t.getStartStation().getName() + " -> " + t.getEndStation().getName())
                    .seatInfo(t.getSeat().getCarriage().getCarriageNumber() + "호차 " + t.getSeat().getSeatNumber())
                    .trainInfo(t.getSchedule().getTrain().getTrainType() + " " + t.getSchedule().getTrain().getTrainNumber())
                    .status(t.getReservation().getStatus().name())
                    .build();
        }
    }
}
