package com.portfolio.food_delivery.domain.review.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class ReviewNotFoundException extends BusinessException {

    public ReviewNotFoundException() {
        super(ErrorCode.ENTITY_NOT_FOUND);
    }

    public ReviewNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }
}