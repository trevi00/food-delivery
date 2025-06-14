package com.portfolio.food_delivery.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("대기중"),
    PROCESSING("처리중"),
    SUCCESS("성공"),
    FAILED("실패"),
    CANCELLED("취소"),
    PARTIAL_CANCELLED("부분취소");

    private final String description;
}