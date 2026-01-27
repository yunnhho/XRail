package com.dev.XRail.infra.kafka;

import com.dev.XRail.domain.reservation.dto.ReservationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "reservation_complete";

    public void sendReservationComplete(ReservationEvent event) {
        log.info("Kafka Event 발행 시작: reservationId={}", event.getReservationId());

        // Fire-and-Forget (비동기 전송)
        kafkaTemplate.send(TOPIC, event.getReservationId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka 발행 성공: offset={}", result.getRecordMetadata().offset());
                    } else {
                        // 실패 시 재시도 로직이나 DLQ(Dead Letter Queue) 처리 필요
                        log.error("Kafka 발행 실패: {}", ex.getMessage());
                    }
                });
    }
}