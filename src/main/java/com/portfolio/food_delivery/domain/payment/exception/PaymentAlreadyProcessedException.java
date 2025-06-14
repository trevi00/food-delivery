package com.portfolio.food_delivery.domain.payment.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class PaymentAlreadyProcessedException extends BusinessException {
    public PaymentAlreadyProcessedException(String message) {
        super(ErrorCode.PAYMENT_ALREADY_PROCESSED, message);
    }
}