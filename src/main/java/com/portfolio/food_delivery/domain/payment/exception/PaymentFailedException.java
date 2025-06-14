package com.portfolio.food_delivery.domain.payment.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class PaymentFailedException extends BusinessException {
    public PaymentFailedException(String message) {
        super(ErrorCode.PAYMENT_FAILED, message);
    }
}