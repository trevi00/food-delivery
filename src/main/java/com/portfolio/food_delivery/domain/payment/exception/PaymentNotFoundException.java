package com.portfolio.food_delivery.domain.payment.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class PaymentNotFoundException extends BusinessException {
    public PaymentNotFoundException() {
        super(ErrorCode.PAYMENT_NOT_FOUND);
    }

    public PaymentNotFoundException(String message) {
        super(ErrorCode.PAYMENT_NOT_FOUND, message);
    }
}