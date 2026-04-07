package com.eswar.productservice.constatnts;

import lombok.Getter;

@Getter
public enum ProductColor {

    RED("Red"),
    BLUE("Blue"),
    GREEN("Green"),
    BLACK("Black"),
    WHITE("White"),
    GREY("Grey"),
    YELLOW("Yellow"),
    ORANGE("Orange");

    private final String displayName;

    ProductColor(String displayName) {
        this.displayName = displayName;
    }
}
