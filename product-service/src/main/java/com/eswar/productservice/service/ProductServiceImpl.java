package com.eswar.productservice.service;

import com.eswar.productservice.constatnts.ProductStatus;
import com.eswar.productservice.dto.*;
import com.eswar.productservice.entity.*;
import com.eswar.productservice.exception.*;
import com.eswar.productservice.mapper.IProductMapper;
import com.eswar.productservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final IProductMapper mapper;

    @Override
    @Transactional
    public ProductResponseDto create(CreateProductRequestDto request) {

        log.info("Creating product with SKU: {}", request.sku());

        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }

        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND,request.categoryId()));

        ProductEntity product = mapper.toEntity(request);

        product.setCategory(category);
        product.setStatus(ProductStatus.ACTIVE);

        ProductEntity saved = productRepository.save(product);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getById(UUID id) {
//fetch all
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,id));

        return mapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDto> getAll(Pageable pageable) {


        Page<ProductEntity> page=productRepository.findAll(pageable);

        List<ProductResponseDto> content=page.getContent().stream().map(
                mapper::toResponse
        ).toList();



        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public ProductResponseDto update(UUID id, UpdateProductRequestDto request) {
        //fetch
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());

        return mapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        //fetch
      productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,id));


        productRepository.deleteById(id);
    }
}