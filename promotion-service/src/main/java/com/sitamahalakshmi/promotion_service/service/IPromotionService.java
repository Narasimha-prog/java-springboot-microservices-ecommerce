package com.sitamahalakshmi.promotion_service.service;

import com.sitamahalakshmi.promotion_service.kafka.event.ProductCreatedEvent;

public interface IPromotionService {
    void handleProductCreatedEvent(ProductCreatedEvent event);
}
