package com.eswar.userservice.exception;

import com.eswar.userservice.constants.ErrorMessages;


public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String message) {
        super(message,"USER_NOT_FOUND");
    }
}
