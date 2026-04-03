package com.eswar.inventoryservice.repository;

import com.eswar.inventoryservice.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IEventRepository extends JpaRepository<EventEntity, UUID> {
}