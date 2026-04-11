package com.eswar.productservice.mapper;

import com.eswar.productservice.dto.CreateProductRequestDto;
import com.eswar.productservice.dto.ProductResponseDto;
import com.eswar.productservice.dto.UpdateProductRequestDto;
import com.eswar.productservice.entity.PictureEntity;
import com.eswar.productservice.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {


    @Value("${file.storage.base-url:http://localhost:8083/uploads/}")


    protected String fileBaseUrl;
    // ADD THIS: Maps Update DTO to the existing Entity
    @Mapping(target = "id", ignore = true) // Don't let the DTO change the ID
    @Mapping(target = "pictureEntities", ignore = true) // Handle pictures separately in service
    @Mapping(target = "category", ignore = true) // Handle category lookup in service
    public abstract void updateEntityFromDto(UpdateProductRequestDto dto, @MappingTarget ProductEntity entity);

    public abstract ProductEntity toEntity(CreateProductRequestDto request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "imageUrls", source = "pictureEntities", qualifiedByName = "mapPicturesToUrls")
    @Mapping(target ="productSize" ,source = "productSize")
    @Mapping(target = "productColor",source = "productColor")
    public abstract ProductResponseDto toResponse(ProductEntity product);

    @Named("mapPicturesToUrls")
    protected List<String> mapPicturesToUrls(Set<PictureEntity> pictures) {
        if (pictures == null || pictures.isEmpty()) {
            return List.of();
        }

        return pictures.stream()
                .map(pic -> fileBaseUrl + pic.getStorageKey()) // Attaches the base URL
                .collect(Collectors.toList());
    }
}