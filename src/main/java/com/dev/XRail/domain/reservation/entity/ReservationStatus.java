package com.dev.XRail.domain.reservation.entity;

public enum ReservationStatus {
    PENDING,   // 결제 대기 (Redis 점유 상태)
    PAID,      // 결제 완료 (최종 확정)
    CANCELLED  // 취소됨
}