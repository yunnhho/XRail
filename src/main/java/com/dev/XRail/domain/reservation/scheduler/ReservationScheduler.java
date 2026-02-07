package com.dev.XRail.domain.reservation.scheduler;

import com.dev.XRail.domain.reservation.entity.Reservation;
import com.dev.XRail.domain.reservation.entity.ReservationStatus;
import com.dev.XRail.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    /**
     * 1분마다 실행: 20분이 지난 PENDING 상태의 예약을 취소 처리
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(20);
        List<Reservation> expiredOnes = reservationRepository.findAllByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING, threshold);

        if (!expiredOnes.isEmpty()) {
            log.info("⏰ 결제 기한 만료 예약 {}건 취소 처리", expiredOnes.size());
            for (Reservation res : expiredOnes) {
                res.updateStatus(ReservationStatus.CANCELLED);
            }
        }
    }
}
