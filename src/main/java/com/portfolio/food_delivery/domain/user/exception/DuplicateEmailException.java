package com.portfolio.food_delivery.domain.user.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }

    public DuplicateEmailException(String message) {
        super(ErrorCode.EMAIL_DUPLICATION, message);
    }
}