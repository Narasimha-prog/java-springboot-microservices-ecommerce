package com.eswar.productservice.exception;

public class InvalidUserIdException extends BusinessException {
    public InvalidUserIdException(String userId) {
        super("Invalid user id: " + userId, "INVALID_USER_ID");
    }
}
