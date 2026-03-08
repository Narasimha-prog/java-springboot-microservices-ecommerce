package com.eswar.inventoryservice.mapper;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.entity.InventoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IInventoryMapper {
    InventoryDto toDto(InventoryEntity inventoryEntity);
    InventoryEntity toEntity(InventoryDto inventoryDto);
}
