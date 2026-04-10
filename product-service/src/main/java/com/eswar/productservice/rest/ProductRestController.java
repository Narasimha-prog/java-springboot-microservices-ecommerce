package com.eswar.productservice.rest;

import com.eswar.productservice.constatnts.ProductSize;
import com.eswar.productservice.dto.*;
import com.eswar.productservice.service.IProductService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Service",description = "to create products in ecommerce")
public class ProductRestController {

    private final IProductService service;
//for admin
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDto create(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @Valid @RequestPart("product") CreateProductRequestDto request,
            @RequestPart("files") List<MultipartFile> files) {

        return service.create(request,files);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequestDto request) {
        return ResponseEntity.ok( service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }


    // --- Shop/Discovery Operations  ---

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getById(@PathVariable UUID id) {
        return   ResponseEntity.ok(service.getById(id));
    }
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponseDto>> getAllProducts(
            @ParameterObject Pageable pageable) {
        return  ResponseEntity.ok( service.getAll(pageable));
    }
    @GetMapping("/featured")
    public ResponseEntity<PageResponse<ProductResponseDto>> getFeaturedProducts(
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 10) Pageable pageable) {

        // Assuming your service returns a PageResponse or Page
        PageResponse<ProductResponseDto> featuredProducts = service.getFeatured(pageable);
        return ResponseEntity.ok(featuredProducts);
    }

    @GetMapping("/related")
    public ResponseEntity<PageResponse<ProductResponseDto>> relatedProducts(
            @ParameterObject Pageable pageable,
            @RequestParam("publicId") UUID id) {

        PageResponse<ProductResponseDto> related = service.getRelated(pageable, id);
        return ResponseEntity.ok(related);
    }

    @GetMapping("/filter")
    public ResponseEntity<PageResponse<ProductResponseDto>> filter(
            @ParameterObject Pageable pageable,
            @RequestParam("categoryId") UUID categoryId,
            @RequestParam(value = "productSizes", required = false) List<ProductSize> productSizes) {

        // If you use a QueryBuilder like the template, it would be initialized here
        PageResponse<ProductResponseDto> filteredProducts = service.filter(pageable, categoryId, productSizes);

        return ResponseEntity.ok(filteredProducts);
    }
}