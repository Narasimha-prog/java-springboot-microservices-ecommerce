package com.eswar.productservice.service;

import com.eswar.productservice.dto.*;
import com.eswar.productservice.entity.CategoryEntity;
import com.eswar.productservice.exception.*;
import com.eswar.productservice.mapper.ICategoryMapper;
import com.eswar.productservice.repository.ICategoryRepository;
import com.eswar.productservice.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements ICategoryService {

    private final ICategoryRepository repository;
    private final ICategoryMapper mapper;

    @Override
    @Transactional
    public CategoryResponseDto create(CategoryRequestDto request) {

        log.info("Creating category: {}", request.name());

        if (repository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS, request.name());
        }

        CategoryEntity category = mapper.toEntity(request);
        CategoryEntity saved = repository.save(category);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getById(UUID id) {

        CategoryEntity category = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND,id));

        return mapper.toResponse(category);
    }

    @Override
    public List<CategoryResponseDto> getAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponseDto update(UUID id, CategoryRequestDto request) {


        CategoryEntity category = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND,id));


        category.setName(request.name());
        category.setDescription(request.description());

        return mapper.toResponse(repository.save(category));
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND,id));


        repository.deleteById(id);
    }
}