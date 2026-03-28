package com.eswar.productservice.exception;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException(String message) {
        super(message,"PRODUCT_NOT_FOUND");
    }
}
