package com.sitamahalakshmi.review_service.mapper;

import com.sitamahalakshmi.review_service.dto.ReviewRequestDto;
import com.sitamahalakshmi.review_service.dto.ReviewResponseDto;
import com.sitamahalakshmi.review_service.entity.ReviewEntity;
import com.sitamahalakshmi.review_service.entity.ReviewImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ReviewMapper {

    @Value("${file.storage.base-url:http://localhost:8083/uploads/}")
    protected String fileBaseUrl;

    public abstract ReviewEntity toEntity(ReviewRequestDto request);

    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "mapReviewImagesToUrls")
    public abstract ReviewResponseDto toResponse(ReviewEntity entity);


    @Named("mapReviewImagesToUrls")
    protected List<String> mapReviewImagesToUrls(List<ReviewImageEntity> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .map(img -> fileBaseUrl + img.getStorageKey())
                .collect(Collectors.toList());
    }
}
