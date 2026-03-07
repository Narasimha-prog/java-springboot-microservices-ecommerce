package com.eswar.inventoryservice.repository;

import com.eswar.inventoryservice.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IInventoryRepository extends JpaRepository<InventoryEntity, UUID> {
}
