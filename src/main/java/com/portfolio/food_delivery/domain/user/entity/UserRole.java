package com.portfolio.food_delivery.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER("고객"),
    RESTAURANT_OWNER("레스토랑 사장"),
    DELIVERY_PARTNER("배달 파트너"),
    ADMIN("관리자");

    private final String description;
}