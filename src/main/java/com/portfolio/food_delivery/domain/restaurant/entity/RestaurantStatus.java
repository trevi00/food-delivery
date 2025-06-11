package com.portfolio.food_delivery.domain.restaurant.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestaurantStatus {
    OPEN("영업중"),
    CLOSED("영업종료"),
    TEMPORARILY_CLOSED("임시휴업"),
    PERMANENTLY_CLOSED("폐업");

    private final String description;
}