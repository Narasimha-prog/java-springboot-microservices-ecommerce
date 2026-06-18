package com.eswar.productservice.dto;

import java.io.Serializable;

public record PictureDto(
        byte[] file, String mineType
) implements Serializable {
}
