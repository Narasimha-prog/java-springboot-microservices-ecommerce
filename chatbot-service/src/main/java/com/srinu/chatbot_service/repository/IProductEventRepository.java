package com.srinu.chatbot_service.repository;

import com.srinu.chatbot_service.entity.ProductEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IProductEventRepository extends JpaRepository<ProductEventEntity, UUID> {
}
