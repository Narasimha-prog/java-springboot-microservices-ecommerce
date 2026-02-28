package com.eswar.userservice.exception;

import com.eswar.userservice.constants.ErrorMessages;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super(ErrorMessages.USER_NOT_FOUND.format(id));
    }
}
