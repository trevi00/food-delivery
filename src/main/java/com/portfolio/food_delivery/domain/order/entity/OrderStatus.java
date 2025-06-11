package com.portfolio.food_delivery.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("주문 대기"),
    CONFIRMED("주문 확인"),
    PREPARING("조리중"),
    READY("조리 완료"),
    DELIVERING("배달중"),
    DELIVERED("배달 완료"),
    CANCELLED("취소됨");

    private final String description;
}