package com.portfolio.food_delivery.domain.restaurant.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestaurantCategory {
    KOREAN("한식"),
    JAPANESE("일식"),
    CHINESE("중식"),
    WESTERN("양식"),
    CHICKEN("치킨"),
    PIZZA("피자"),
    BURGER("버거"),
    CAFE("카페/디저트"),
    ASIAN("아시안"),
    SNACK("분식"),
    MIDNIGHT("야식"),
    OTHER("기타");

    private final String description;
}