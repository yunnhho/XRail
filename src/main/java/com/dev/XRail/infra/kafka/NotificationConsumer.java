package com.dev.XRail.infra.kafka;

import com.dev.XRail.domain.reservation.dto.ReservationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "reservation_complete", groupId = "XRail-group")
    public void handleReservationComplete(Object message) {
        try {
            // 역직렬화 (LinkedHashMap으로 들어오는 경우 대비 안전한 변환)
            ReservationEvent event;
            if (message instanceof LinkedHashMap) {
                event = objectMapper.convertValue(message, ReservationEvent.class);
            } else {
                event = (ReservationEvent) message;
            }

            log.info("=========== [알림톡 발송 시뮬레이션] ===========");
            log.info("수신자 ID: {}", event.getUserId());
            log.info("예약 정보: [열차 {}] {} / 좌석 {}",
                    event.getTrainNumber(), event.getDepartureDate(), event.getSeatNumber());
            log.info("==============================================");

            // 실제 SMS/Email 발송 서비스 호출 시뮬레이션 완료

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}