package com.dev.XRail.domain.reservation.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import com.dev.XRail.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Long totalPrice; // 총 결제 금액

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt; // 예매 확정 일시

    // Cascade: 예매가 삭제되면 티켓도 같이 삭제 (단, 실무에선 Soft Delete 권장)
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    @Builder
    public Reservation(User user, ReservationStatus status, Long totalPrice, LocalDateTime reservedAt) {
        this.user = user;
        this.status = status;
        this.totalPrice = totalPrice;
        this.reservedAt = reservedAt;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}