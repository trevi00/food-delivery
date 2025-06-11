package com.portfolio.food_delivery.domain.restaurant.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class RestaurantNotFoundException extends BusinessException {

    public RestaurantNotFoundException() {
        super(ErrorCode.RESTAURANT_NOT_FOUND);
    }

    public RestaurantNotFoundException(String message) {
        super(ErrorCode.RESTAURANT_NOT_FOUND, message);
    }
}