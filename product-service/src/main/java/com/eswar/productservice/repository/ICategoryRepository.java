package com.eswar.productservice.repository;

import com.eswar.productservice.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ICategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    boolean existsByName(String name);

}
