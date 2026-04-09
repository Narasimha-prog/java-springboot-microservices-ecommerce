package com.eswar.productservice.service;

import com.eswar.productservice.dto.CreateProductRequestDto;
import com.eswar.productservice.dto.PageResponse;
import com.eswar.productservice.dto.ProductResponseDto;
import com.eswar.productservice.dto.UpdateProductRequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.plaf.multi.MultiListUI;
import java.util.List;
import java.util.UUID;

public interface IProductService {

    ProductResponseDto create(CreateProductRequestDto request, List<MultipartFile> files);

    ProductResponseDto getById(UUID id);

    PageResponse<ProductResponseDto> getAll(Pageable pageable);

    ProductResponseDto update(UUID id, UpdateProductRequestDto request);

    void delete(UUID id);

    PageResponse<ProductResponseDto> getFeatured(Pageable pageable);

    PageResponse<ProductResponseDto> getRelated(Pageable pageable, UUID id);

    PageResponse<ProductResponseDto> filter(Pageable pageable, UUID categoryId, List<String> productSizes);
}
