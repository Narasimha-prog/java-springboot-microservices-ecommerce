package com.eswar.inventoryservice.service;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.dto.PageResponse;
import com.eswar.inventoryservice.entity.EventEntity;
import com.eswar.inventoryservice.entity.InventoryEntity;
import com.eswar.inventoryservice.exception.BusinessException;
import com.eswar.inventoryservice.exception.ErrorCode;
import com.eswar.inventoryservice.kafka.constants.EventStatus;
import com.eswar.inventoryservice.kafka.constants.EventType;
import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;
import com.eswar.inventoryservice.kafka.event.OrderItem;
import com.eswar.inventoryservice.kafka.service.InventoryKafkaService;
import com.eswar.inventoryservice.mapper.IInventoryMapper;
import com.eswar.inventoryservice.repository.IEventRepository;
import com.eswar.inventoryservice.repository.IInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class  InventoryServiceImp implements IInventoryService{

    private  final IInventoryRepository inventoryRepository;
    private final IInventoryMapper  inventoryMapper;
    private final IEventRepository eventRepository;
    private final InventoryKafkaService kafkaService;

    @Autowired
    @Lazy
    private InventoryServiceImp self;

    @Transactional
    public void handleOrderCreatedEvent(@NonNull OrderCreatedEvent event) {

        // Step A: Record the Receipt (Always Commits)
        EventEntity eventEntity = self.recordReceipt(event);

        // Step B: Skip if already processed (Idempotency)
        if (eventEntity.getStatus() == EventStatus.PROCESSED) {
            log.info("Event {} already processed. Skipping business logic.", event.eventId());
            return;
        }


        try {
            // Step C: Risky Business Logic
            self.processInventory(event, eventEntity);
        } catch (Exception ex) {
            log.warn("error while handling process inventory",ex);
            // Step D: Record Failure (Always Commits)
            self.recordFailure(eventEntity, ex.getMessage());
        }
    }

//for process
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processInventory(OrderCreatedEvent event, EventEntity entity) {

        Set<InventoryEntity> toUpdate = new LinkedHashSet<>();

        for (OrderItem item : event.items()) {
            InventoryEntity inv = inventoryRepository.findById(item.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND, item.productId()));

            if (inv.getAvailableQuantity() < item.quantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, item.productId());
            }

            inv.setAvailableQuantity(inv.getAvailableQuantity() - item.quantity());
            inv.setReservedQuantity(inv.getReservedQuantity() + item.quantity());
            toUpdate.add(inv);
        }

        inventoryRepository.saveAll(toUpdate);
        entity.setStatus(EventStatus.PROCESSED);
        eventRepository.save(entity);

        // Success notification
        kafkaService.sendOrderStatusEvent(entity, true, "Stock Reserved");
    }

    //for failure
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(EventEntity entity, String error) {

        entity.setStatus(EventStatus.FAILED);
        entity.setErrorMessage(error);
        eventRepository.save(entity);

        // Failure notification
        kafkaService.sendOrderStatusEvent(entity, false, error);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventEntity recordReceipt(OrderCreatedEvent event) {
        return eventRepository.findById(event.eventId())
                .map(existing -> {
                    log.info("Retry detected for event {}. Generating NEW attempt ID.", event.eventId());
                    // 🔹 Always add a fresh UUID to track THIS specific retry attempt
                    boolean isNewTrace = existing.getTraceIds().add(event.traceId());
                    if (!isNewTrace) {
                        log.info("Duplicate traceId {} detected. Generating supplemental ID.", event.traceId());
                        existing.getTraceIds().add(UUID.randomUUID());
                    }
                    return eventRepository.save(existing);
                })
                .orElseGet(() -> {
                    log.info("First attempt for event {}. Initializing trace set.", event.eventId());

                    // 🔹 Create with the original traceId from the event
                    EventEntity newEntity = EventEntity.builder()
                            .eventId(event.eventId())
                            .traceIds(new HashSet<>(Set.of(event.traceId())))
                            .orderId(event.orderId())
                            .eventType(EventType.INVENTORY)
                            .status(EventStatus.RECEIVED)
                            .payload(event.toString())
                            .build();
                    return eventRepository.save(newEntity);
                });
    }
    @Transactional
    public InventoryDto createInventory(InventoryDto dto) {

         if(inventoryRepository.existsById(dto.productId())){
             throw  new BusinessException(ErrorCode.INVENTORY_ALREADY_EXISTS,dto.productId());
         }
        InventoryEntity entity = inventoryMapper.toEntity(dto);
        InventoryEntity saved = inventoryRepository.save(entity);

        return inventoryMapper.toDto(saved);
    }


    @Transactional(readOnly = true)
    public InventoryDto getInventory(UUID productId) {

        InventoryEntity entity = inventoryRepository
                .findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,productId));

        return inventoryMapper.toDto(entity);
    }


    @Transactional(readOnly = true)
    @Override
    public PageResponse<InventoryDto> getAllInventories(Pageable pageable) {

        Page<InventoryEntity> page =inventoryRepository.findAll(pageable);

        List<InventoryDto> content=page.getContent().stream().map(inventoryMapper::toDto).toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    @Override
    @Transactional
    public InventoryDto updateInventory(UUID productId, InventoryDto dto) {

        InventoryEntity inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,productId));

        // Logic: Admin sets the new totals
        inventoryMapper.updateEntityFromDto(dto,inventory);
        InventoryEntity savedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.toDto(savedInventory);
    }


}
