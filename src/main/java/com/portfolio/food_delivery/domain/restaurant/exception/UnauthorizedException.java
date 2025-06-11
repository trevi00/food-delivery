package com.portfolio.food_delivery.domain.restaurant.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ErrorCode.RESTAURANT_UNAUTHORIZED, message);
    }
}