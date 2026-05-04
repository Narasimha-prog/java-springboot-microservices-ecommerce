package com.sitamahalakshmi.review_service.dto;

import java.util.UUID;

public record ReviewRequestDto(
        UUID productId,
        UUID userId,
        int rating,
        String comment
) {}
