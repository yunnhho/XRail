package com.dev.XRail.common;

import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import com.dev.XRail.domain.station.entity.Route;
import com.dev.XRail.domain.station.entity.RouteStation;
import com.dev.XRail.domain.station.entity.Station;
import com.dev.XRail.domain.station.repository.RouteRepository;
import com.dev.XRail.domain.station.repository.RouteStationRepository;
import com.dev.XRail.domain.station.repository.StationRepository;
import com.dev.XRail.domain.train.entity.Carriage;
import com.dev.XRail.domain.train.entity.Seat;
import com.dev.XRail.domain.train.entity.Train;
import com.dev.XRail.domain.train.entity.TrainType;
import com.dev.XRail.domain.train.repository.CarriageRepository;
import com.dev.XRail.domain.train.repository.SeatRepository;
import com.dev.XRail.domain.train.repository.TrainRepository;
import com.dev.XRail.domain.reservation.repository.ReservationRepository;
import com.dev.XRail.domain.reservation.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;
    private final RouteStationRepository routeStationRepository;
    private final TrainRepository trainRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final CarriageRepository carriageRepository;
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========== [DataInitializer] 거리 및 요금 기반 데이터 생성 시작 ==========");
        
        long scheduleCount = scheduleRepository.count();
        if (scheduleCount < 1000) { // 강제 초기화 유도 (전체 스케줄 약 1400개 예상)
            log.info("[DataInitializer] 데이터가 부족하거나 구버전입니다. 전체 초기화를 진행합니다.");
            clearAllData();
            initAllData();
        } else {
            log.info("[DataInitializer] 이미 데이터가 충분합니다 (count: {}). 건너뜁니다.", scheduleCount);
        }
        log.info("========== [DataInitializer] 데이터 작업 완료 ==========");
    }

    private void clearAllData() {
        log.info("[DataInitializer] 기존 데이터 삭제 중...");
        ticketRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        carriageRepository.deleteAllInBatch();
        trainRepository.deleteAllInBatch();
        routeStationRepository.deleteAllInBatch();
        routeRepository.deleteAllInBatch();
        stationRepository.deleteAllInBatch();
    }

    private void initAllData() {
        Map<String, Station> stationMap = new HashMap<>();
        List<String> allStationNames = Arrays.asList(
            "서울", "광명", "천안아산", "오송", "대전", "김천구미", "동대구", "경주", "울산", "부산",
            "수원", "평택", "서대전", "익산", "정읍", "광주송정", "나주", "목포",
            "전주", "남원", "곡성", "구례구", "순천", "여수EXPO"
        );
        for (String name : allStationNames) {
            stationMap.put(name, stationRepository.save(new Station(name)));
        }

        log.info("[DataInitializer] 노선 생성 중...");
        createRoute("경부선", Arrays.asList("서울", "광명", "천안아산", "오송", "대전", "김천구미", "동대구", "경주", "울산", "부산"), 
                    Arrays.asList(0.0, 22.0, 96.0, 142.0, 159.0, 230.0, 282.0, 331.0, 361.0, 417.0), stationMap);
        
        createRoute("호남선", Arrays.asList("서울", "수원", "평택", "천안아산", "익산", "정읍", "광주송정", "나주", "목포"), 
                    Arrays.asList(0.0, 41.0, 78.0, 115.0, 214.0, 257.0, 303.0, 319.0, 386.0), stationMap);

        createRoute("전라선", Arrays.asList("서울", "오송", "익산", "전주", "남원", "곡성", "구례구", "순천", "여수EXPO"), 
                    Arrays.asList(0.0, 142.0, 214.0, 232.0, 285.0, 305.0, 323.0, 359.0, 393.0), stationMap);

        log.info("[DataInitializer] 열차 및 좌석 생성 중...");
        List<Train> trains = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Train train = trainRepository.save(Train.builder()
                    .trainType(TrainType.KTX)
                    .trainNumber("KTX-" + (100 + i))
                    .build());
            createSeats(train);
            trains.add(train);
        }

        log.info("[DataInitializer] 스케줄 생성 중...");
        createSchedules(trains);
    }

    private void createRoute(String routeName, List<String> names, List<Double> distances, Map<String, Station> stationMap) {
        Route route = routeRepository.save(new Route(routeName));
        for (int i = 0; i < names.size(); i++) {
            routeStationRepository.save(RouteStation.builder()
                    .route(route)
                    .station(stationMap.get(names.get(i)))
                    .stationSequence(i) // 비트마스킹을 위해 0부터 순차 할당
                    .cumulativeDistance(distances.get(i))
                    .build());
        }
    }

    private void createSeats(Train train) {
        for (int carNum = 1; carNum <= 8; carNum++) {
            Carriage carriage = carriageRepository.save(Carriage.builder()
                    .train(train)
                    .carriageNumber(carNum)
                    .seatCount(20)
                    .build());
            
            // 4열 시트 (A, B, C, D) * 5줄 = 20석
            for (int row = 1; row <= 5; row++) {
                String[] cols = {"A", "B", "C", "D"};
                for (String col : cols) {
                    seatRepository.save(Seat.builder()
                            .carriage(carriage)
                            .seatNumber(row + col)
                            .build());
                }
            }
        }
    }

    private void createSchedules(List<Train> trains) {
        List<Route> routes = routeRepository.findAll();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate date = today.plusDays(i);
            for (Route route : routes) {
                List<RouteStation> rss = routeStationRepository.findByRouteIdOrderByStationSequenceAsc(route.getId());
                double totalDist = rss.get(rss.size()-1).getCumulativeDistance();
                int travelMinutes = (int) (totalDist / 200.0 * 60.0); // 200km/h 가정

                for (int hour = 6; hour <= 22; hour += 2) {
                    Train train = trains.get((date.getDayOfMonth() + hour) % trains.size());
                    LocalTime dep = LocalTime.of(hour, 0);
                    scheduleRepository.save(Schedule.builder()
                            .train(train).route(route).departureDate(date).departureTime(dep).arrivalTime(dep.plusMinutes(travelMinutes)).build());
                }
            }
        }
    }
}
