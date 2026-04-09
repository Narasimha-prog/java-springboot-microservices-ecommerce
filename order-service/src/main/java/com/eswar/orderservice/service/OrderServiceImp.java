package com.eswar.orderservice.service;

import com.eswar.grpc.user.ProductResponse;
import com.eswar.orderservice.constants.OrderStatus;
import com.eswar.orderservice.dto.OrderDto;
import com.eswar.orderservice.dto.OrderItemDto;
import com.eswar.orderservice.dto.OrderResponseDto;
import com.eswar.orderservice.dto.PageResponse;
import com.eswar.orderservice.entity.EventEntity;
import com.eswar.orderservice.entity.OrderEntity;
import com.eswar.orderservice.entity.OrderedItemEntity;
import com.eswar.orderservice.entity.OrderedItemId;
import com.eswar.orderservice.exceptions.BusinessException;
import com.eswar.orderservice.exceptions.ErrorCode;
import com.eswar.orderservice.grpc.client.GrpcProductServiceClient;
import com.eswar.orderservice.grpc.mapper.GrpcExceptionMapper;
import com.eswar.orderservice.kafka.constatnts.EventStatus;
import com.eswar.orderservice.kafka.constatnts.EventType;
import com.eswar.orderservice.kafka.event.OrderCreatedEvent;
import com.eswar.orderservice.kafka.event.OrderItemEvent;
import com.eswar.orderservice.kafka.event.OrderStatusEvent;
import com.eswar.orderservice.kafka.producer.OrderEventProducer;
import com.eswar.orderservice.kafka.service.OrderKafkaService;
import com.eswar.orderservice.mapper.IOrderMapper;
import com.eswar.orderservice.repository.IEventRepository;
import com.eswar.orderservice.repository.IOrderRepository;
import com.eswar.orderservice.service.IOrderService;
import com.eswar.orderservice.util.PagedUtils;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImp implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IEventRepository eventRepository;
    private  final IOrderMapper mapper;
     private final OrderKafkaService orderKafkaService;
    private  final GrpcProductServiceClient grpcProductServiceClient;

    @Autowired
    @Lazy
    private OrderServiceImp self;


    @Transactional
    public void handleOrderStatusEvent(OrderStatusEvent event) {

        // Step A: Record Receipt (Independent Transaction)
        // This ensures we have a record even if the rest fails
        EventEntity eventEntity = self.recordEventReceipt(event);

        // Step B: Idempotency Check
        if (eventEntity.getStatus() == EventStatus.PROCESSED) {
            log.info("Event {} already processed. Skipping.", event.eventId());
            return;
        }

        try {
            // Step C: Business Logic (Independent Transaction)
            self.applyStatusUpdate(event, eventEntity);
            log.info("Successfully updated order {} to status {}", event.orderId(), event.status());
        } catch (Exception ex) {
            // Step D: Record Failure (Independent Transaction)
            self.recordEventFailure(eventEntity, ex.getMessage());
            // We rethrow to trigger Kafka retry if necessary
            throw ex;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventEntity recordEventReceipt(OrderStatusEvent event) {
        return eventRepository.findById(event.eventId())
                .orElseGet(() -> {
                    EventEntity entity = EventEntity.builder()
                            .eventId(event.eventId())
                            .orderId(event.orderId())
                            .eventType(event.eventType())
                            .status(EventStatus.RECEIVED)
                            .payload(event.toString())
                            .build();
                    return eventRepository.save(entity);
                });
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyStatusUpdate(OrderStatusEvent event, EventEntity entity) {
        // Call your existing business logic
        updateOrderStatus(
                event.orderId(),
                event.eventType(),
                event.status(),
                event.paymentReference()
        );

        entity.setStatus(EventStatus.PROCESSED);
        eventRepository.save(entity);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEventFailure(EventEntity entity, String error) {
        entity.setStatus(EventStatus.FAILED);
        entity.setErrorMessage(error);
        eventRepository.save(entity);
    }
    // ================= HELPER =================

    private UUID parseUUID(String id, ErrorCode errorCode) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new BusinessException(errorCode);
        }
    }

    private UUID extractUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return parseUUID(principal.getName(), ErrorCode.INVALID_USER_ID);
    }

    // ================= CREATE ORDER =================

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderDto dto, Principal principal) {

        UUID customerId = extractUserId(principal);

        OrderEntity order = mapper.toEntity(dto);
        order.setStatus(OrderStatus.CREATED);
        order.setCustomerId(customerId);

        Set<OrderedItemEntity> items = new LinkedHashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : dto.items()) {

            var product = getProductOrThrow(itemDto.productId());

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemDto.quantity()));

            totalAmount = totalAmount.add(itemTotal);

            OrderedItemId id = new OrderedItemId();
            id.setOrder(order);
            id.setProductId(itemDto.productId());

            OrderedItemEntity item = new OrderedItemEntity();
            item.setId(id);
            item.setQuantity(itemDto.quantity());
            item.setPrice(price);

            items.add(item);
        }

        order.setItems(items);

        OrderEntity saved = orderRepository.save(order);

        orderKafkaService.sendOrderCreatedEvent(saved, totalAmount);

        return mapper.toResponse(saved);
    }

    // ================= GET =================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponseDto> getALlOrders(Pageable pageable) {
        return PagedUtils.toPageResponse(orderRepository.findAll(pageable),mapper::toResponse);
    }

    @Override
    public OrderResponseDto getOrderById(String orderId) {

        UUID id = parseUUID(orderId, ErrorCode.ORDER_INVALID_ID);

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return mapper.toResponse(order);
    }

    // ================= UPDATE =================

    @Override
    @Transactional
    public OrderResponseDto updateOrder(String orderId, OrderDto dto) {

        UUID id = parseUUID(orderId, ErrorCode.ORDER_INVALID_ID);

        OrderEntity existing = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (existing.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_UPDATE);
        }

        Set<OrderedItemEntity> items = new LinkedHashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : dto.items()) {

            var product = getProductOrThrow(itemDto.productId());

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemDto.quantity()));

            totalAmount = totalAmount.add(itemTotal);

            OrderedItemId itemId = new OrderedItemId();
            itemId.setOrder(existing);
            itemId.setProductId(itemDto.productId());

            OrderedItemEntity item = new OrderedItemEntity();
            item.setId(itemId);
            item.setQuantity(itemDto.quantity());
            item.setPrice(price);

            items.add(item);
        }

        existing.setItems(items);
        existing.setStatus(OrderStatus.CONFIRMED);

        OrderEntity saved = orderRepository.save(existing);

        orderKafkaService.sendOrderCreatedEvent(saved, totalAmount);

        return mapper.toResponse(saved);
    }

    // ================= CANCEL =================

    @Override
    @Transactional
    public void cancelOrder(String orderId) {

        UUID id = parseUUID(orderId, ErrorCode.ORDER_INVALID_ID);

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    // ================= USER =================

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(String orderId, String userId) {

        try {
            UUID orderUUID = UUID.fromString(orderId);
            return orderRepository.findById(orderUUID)
                    .map(o -> o.getCustomerId().toString().equals(userId))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponseDto> getOrdersByCustomerId(String customerId, Pageable pageable) {

        UUID id = parseUUID(customerId, ErrorCode.INVALID_USER_ID);
        return PagedUtils.toPageResponse(orderRepository.findByCustomerId(id,pageable),mapper::toResponse);
    }

    // ================= EVENT HANDLING =================

    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId,
                                  EventType eventType,
                                  EventStatus status,
                                  String paymentReference) {


        // ✅ 3. Fetch order
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // ✅ 4. Business logic
        switch (eventType) {

            case EventType.INVENTORY-> {
                if (EventStatus.PROCESSED.equals(status)) {
                    order.setStatus(OrderStatus.STOCK_RESERVED);
                } else {
                    order.setStatus(OrderStatus.FAILED);
                }
            }

            case EventType.PAYMENT-> {
                if (EventStatus.PROCESSED.equals(status)) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    order.setPaymentReference(paymentReference);
                } else {
                    order.setStatus(OrderStatus.CANCELLED);
                }
            }

            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        orderRepository.save(order);


    }
    // ================= PRIVATE =================

    private ProductResponse getProductOrThrow(UUID productId) {
        try {
            return grpcProductServiceClient.getProduct(productId);
        } catch (StatusRuntimeException e) {
          throw   GrpcExceptionMapper.map(e);
        }
    }


}