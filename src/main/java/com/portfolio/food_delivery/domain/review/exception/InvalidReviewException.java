package com.portfolio.food_delivery.domain.review.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class InvalidReviewException extends BusinessException {

    public InvalidReviewException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}