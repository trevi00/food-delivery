package com.portfolio.food_delivery.domain.menu.exception;

import com.portfolio.food_delivery.common.exception.BusinessException;
import com.portfolio.food_delivery.common.exception.ErrorCode;

public class MenuNotFoundException extends BusinessException {

    public MenuNotFoundException() {
        super(ErrorCode.MENU_NOT_FOUND);
    }

    public MenuNotFoundException(String message) {
        super(ErrorCode.MENU_NOT_FOUND, message);
    }
}