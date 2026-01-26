package com.dev.XRail.domain.train.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrainType {
    KTX("KTX", 1.0),       // 기본 요금 배율 1.0
    KTX_SANCHEON("KTX-산천", 1.0),
    ITX_SAEMAEUL("ITX-새마을", 0.7),
    MUGUNGHWA("무궁화호", 0.5);

    private final String description;
    private final double priceRate; // 요금 할증/할인율
}