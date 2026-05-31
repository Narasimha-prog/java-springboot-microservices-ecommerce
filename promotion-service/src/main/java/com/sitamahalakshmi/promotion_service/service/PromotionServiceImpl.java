package com.sitamahalakshmi.promotion_service.service;

import com.sitamahalakshmi.promotion_service.kafka.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements IPromotionService{
    @Override
    public void handleProductCreatedEvent(ProductCreatedEvent event) {

    }
}
