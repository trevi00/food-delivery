package com.portfolio.food_delivery.domain.cart.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class InvalidCartException extends BusinessException {
    public InvalidCartException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);  // C001 코드로 변경
    }
}