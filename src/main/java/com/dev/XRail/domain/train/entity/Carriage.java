package com.dev.XRail.domain.train.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "carriages") // 객차 (호차)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Carriage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carriage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "carriage_number", nullable = false)
    private Integer carriageNumber; // 1호차, 2호차

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount; // 좌석 수

    @OneToMany(mappedBy = "carriage", cascade = CascadeType.ALL)
    private List<Seat> seats = new ArrayList<>();

    public Carriage(Train train, Integer carriageNumber, Integer seatCount) {
        this.train = train;
        this.carriageNumber = carriageNumber;
        this.seatCount = seatCount;
    }
}