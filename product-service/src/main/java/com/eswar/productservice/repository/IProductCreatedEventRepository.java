package com.eswar.productservice.repository;

import com.eswar.productservice.constatnts.EventStatus;
import com.eswar.productservice.entity.ProductCreatedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IProductCreatedEventRepository extends JpaRepository<ProductCreatedEventEntity, UUID> {

    List<ProductCreatedEventEntity> findByStatusOrderByCreatedAtAsc(EventStatus status);
}
