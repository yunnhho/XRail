package com.dev.XRail.domain.train.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "trains")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Train extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "train_id")
    private Long id;

    @Column(name = "train_number", nullable = false, unique = true)
    private String trainNumber; // 열차 번호 (101, 205...)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainType trainType; // KTX, 새마을 등

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
    private List<Carriage> carriages = new ArrayList<>(); // 1호차, 2호차...

    @Builder
    public Train(String trainNumber, TrainType trainType) {
        this.trainNumber = trainNumber;
        this.trainType = trainType;
    }
}