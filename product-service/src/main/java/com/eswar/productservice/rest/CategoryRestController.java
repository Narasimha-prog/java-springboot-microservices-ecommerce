package com.eswar.productservice.rest;


import com.eswar.productservice.dto.CategoryRequestDto;
import com.eswar.productservice.dto.CategoryResponseDto;
import com.eswar.productservice.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Service",description = "to create category in ecommerce")
public class CategoryRestController {

    private final ICategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "create Product",description = "to create product in ecommerce")
    public CategoryResponseDto create(
            @Valid @RequestBody CategoryRequestDto request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "get Product By Id",description = "using id we need to fetch product")
    public ResponseEntity<CategoryResponseDto> getById(@PathVariable UUID id) {
        return  ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "get  all Products",description = "fetching all products")
    public ResponseEntity<List<CategoryResponseDto>> getAll() {

        return  ResponseEntity.ok( service.getAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "update Product By Id",description = "using id we need to update product")
    public ResponseEntity<CategoryResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequestDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "delete Product By Id",description = "using id we need to delete product")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
