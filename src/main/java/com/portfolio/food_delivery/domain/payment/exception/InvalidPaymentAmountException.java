package com.portfolio.food_delivery.domain.payment.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class InvalidPaymentAmountException extends BusinessException {
    public InvalidPaymentAmountException(String message) {
        super(ErrorCode.INVALID_PAYMENT_AMOUNT, message);
    }
}