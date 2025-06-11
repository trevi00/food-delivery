package com.portfolio.food_delivery.domain.order.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }

    public OrderNotFoundException(String message) {
        super(ErrorCode.ORDER_NOT_FOUND, message);
    }
}