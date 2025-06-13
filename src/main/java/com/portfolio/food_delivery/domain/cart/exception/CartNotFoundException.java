// CartNotFoundException.java
package com.portfolio.food_delivery.domain.cart.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class CartNotFoundException extends BusinessException {

    public CartNotFoundException() {
        super(ErrorCode.ENTITY_NOT_FOUND);
    }

    public CartNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }
}