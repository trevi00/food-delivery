package com.portfolio.food_delivery.domain.order.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class InvalidOrderException extends BusinessException {

    public InvalidOrderException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}