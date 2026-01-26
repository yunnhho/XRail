package com.dev.XRail.domain.train.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "seats") // 물리적 좌석 마스터
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carriage_id", nullable = false)
    private Carriage carriage;

    @Column(name = "seat_number", nullable = false, length = 5)
    private String seatNumber; // 1A, 1B, 2C...

    public Seat(Carriage carriage, String seatNumber) {
        this.carriage = carriage;
        this.seatNumber = seatNumber;
    }
}