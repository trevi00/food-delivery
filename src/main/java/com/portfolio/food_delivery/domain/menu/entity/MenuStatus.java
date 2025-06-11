package com.portfolio.food_delivery.domain.menu.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuStatus {
    AVAILABLE("판매중"),
    SOLD_OUT("품절"),
    HIDDEN("숨김"),
    DELETED("삭제됨");

    private final String description;
}