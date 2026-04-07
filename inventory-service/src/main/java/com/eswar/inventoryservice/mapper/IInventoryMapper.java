package com.eswar.inventoryservice.mapper;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.entity.InventoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IInventoryMapper {
    InventoryDto toDto(InventoryEntity inventoryEntity);
    InventoryEntity toEntity(InventoryDto inventoryDto);
    @Mapping(target = "productId", ignore = true)
    void updateEntityFromDto(InventoryDto dto, @MappingTarget InventoryEntity entity);
}
