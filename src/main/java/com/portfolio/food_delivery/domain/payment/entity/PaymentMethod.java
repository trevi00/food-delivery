package com.portfolio.food_delivery.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    BANK_TRANSFER("계좌이체"),
    KAKAO_PAY("카카오페이"),
    NAVER_PAY("네이버페이"),
    TOSS("토스"),
    CASH("현금"),
    POINT("포인트");

    private final String description;
}