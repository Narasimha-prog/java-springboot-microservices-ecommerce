package com.eswar.productservice.service;

import com.eswar.productservice.constatnts.ProductSize;
import com.eswar.productservice.constatnts.ProductStatus;
import com.eswar.productservice.dto.*;
import com.eswar.productservice.entity.*;
import com.eswar.productservice.exception.*;
import com.eswar.productservice.mapper.ProductMapper;
import com.eswar.productservice.repository.*;
import com.eswar.productservice.util.PagedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final ProductMapper mapper;
    private final IStorageService storageService;


    @Override
    @Transactional
    public ProductResponseDto create(CreateProductRequestDto request, List<MultipartFile> files) {

        log.info("Creating product with SKU: {}", request.sku());

        // 1. Validation
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }

        // 2. Fetch Category & Map Entity FIRST
        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, request.categoryId()));

        ProductEntity product = mapper.toEntity(request);
        product.setCategory(category);
        product.setStatus(ProductStatus.ACTIVE);

        // 3. Handle File Uploads
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String originalName = file.getOriginalFilename();
                String folderPath = "products/" + request.sku();

                // Upload to storage (returns the full storageKey/path)
                String storageKey = storageService.upload(file, folderPath);

                // Logic to extract filename without extension if needed
                String fileNameOnly = (originalName != null && originalName.contains("."))
                        ? originalName.substring(0, originalName.lastIndexOf("."))
                        : originalName;

                PictureEntity picture = PictureEntity.builder()
                        .storagePath(folderPath + "/")
                        .storageKey(storageKey)
                        .fileName(originalName) // Store the full original name
                        .mimeType(file.getContentType())
                        .fileSize(file.getSize())
                        .product(product) // Link to product
                        .build();

                // Link to product (handles bi-directional relationship)
                product.addPicture(picture);
            }
        }

        // 4. Save (CascadeType.ALL will save pictures automatically)
        ProductEntity saved = productRepository.save(product);

        return mapper.toResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getById(UUID id) {

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,id));

        return mapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDto> getAll(Pageable pageable) {
     Page<ProductEntity> entityPage=productRepository.findAll(pageable);

        return PagedUtils.toPageResponse(entityPage,mapper::toResponse);
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

    @Override
    public PageResponse<ProductResponseDto> getFeatured(Pageable pageable) {
        Page<ProductEntity> entityPage=productRepository.findByFeaturedTrue(pageable);

        return PagedUtils.toPageResponse(entityPage,mapper::toResponse);
    }

    @Override
    public PageResponse<ProductResponseDto> getRelated(Pageable pageable, UUID id) {
        Page<ProductEntity> entityPage=productRepository.findByCategoryId(id,pageable);
        return PagedUtils.toPageResponse(entityPage,mapper::toResponse);
    }

    @Override
    public PageResponse<ProductResponseDto> filter(Pageable pageable, UUID categoryId, List<ProductSize> productSizes) {
        Page<ProductEntity> entityPage=productRepository.findByCategoryIdAndProductSizeIn(categoryId,productSizes,pageable);
        return PagedUtils.toPageResponse(entityPage,mapper::toResponse);
    }


}