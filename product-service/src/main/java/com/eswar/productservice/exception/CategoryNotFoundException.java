package com.eswar.productservice.exception;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException(String message) {
        super(message,"CATEGORY_NOT_FOUND");
    }
}
