package com.portfolio.food_delivery.domain.cart.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class CartItemNotFoundException extends BusinessException {
    public CartItemNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }
}
