package com.eswar.productservice.service;

import com.eswar.productservice.dto.CreateProductRequestDto;
import com.eswar.productservice.dto.PageResponse;
import com.eswar.productservice.dto.ProductResponseDto;
import com.eswar.productservice.dto.UpdateProductRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IProductService {

    ProductResponseDto create(CreateProductRequestDto request);

    ProductResponseDto getById(UUID id);

    PageResponse<ProductResponseDto> getAll(Pageable pageable);

    ProductResponseDto update(UUID id, UpdateProductRequestDto request);

    void delete(UUID id);
}
