package com.eswar.userservice.constants;

public enum ErrorMessages {

    USER_NOT_FOUND("User not found with id: %s");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String format(Object value) {
        return String.format(message, value);
    }

}
