package com.lnreddy.cart_service.service;

import com.eswar.grpc.inventory.InventoryResponse;
import com.eswar.grpc.user.ProductResponse;
import com.lnreddy.cart_service.dto.*;
import com.lnreddy.cart_service.entity.*;
import com.lnreddy.cart_service.exceptions.BusinessException;
import com.lnreddy.cart_service.exceptions.ErrorCode;
import com.lnreddy.cart_service.grpc.client.InventoryClient;
import com.lnreddy.cart_service.grpc.mapper.GrpcExceptionMapper;
import com.lnreddy.cart_service.mapper.ICartMapper;
import com.lnreddy.cart_service.repository.ICartRepository;
import com.lnreddy.cart_service.grpc.client.ProductClient;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final ProductClient productClient; // Your gRPC stub wrapper
    private final ICartMapper cartMapper;
    private final InventoryClient inventoryClient;


    @Transactional
    public CartResponseDTO incrementItemQuantity(String userId, String productId) {
        CartEntity cart = findCartOrThrow(userId);
        CartItemEntity item = findItemInCart(cart, productId);

        // gRPC Check: Verify stock before allowing increment
        InventoryResponse inventoryResponse;
        try {
            inventoryResponse = inventoryClient.getProductStock(productId);

        } catch (
                StatusRuntimeException ex) {
            log.warn("gRPC error during login", ex);
            throw GrpcExceptionMapper.map(ex);
        }


        if (item.getQuantity() + 1 > inventoryResponse.getAvailableStock()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, productId);
        }

        item.setQuantity(item.getQuantity() + 1);
        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    @Transactional
    public CartResponseDTO decrementItemQuantity(String userId, String productId) {
        CartEntity cart = findCartOrThrow(userId);
        CartItemEntity item = findItemInCart(cart, productId);

        if (item.getQuantity() <= 1) {
            // Option: Either throw error or just remove the item
            return removeItemFromCart(userId, productId);
        }

        item.setQuantity(item.getQuantity() - 1);
        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public CartResponseDTO addItemToCart(String userId, CartItemRequest request) {
        // 1. Find or Create Cart
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });


        // 2. Check if product already exists in cart
        Optional<CartItemEntity> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst();


        //3 fetch Inventory
        InventoryResponse inventory;
        try {
            log.info("Validating stock for product {} before adding to cart", request.productId());
            inventory = inventoryClient.getProductStock(request.productId().toString());
        } catch (StatusRuntimeException ex) {
            log.error("Failed to fetch inventory for product {}: {}", request.productId(), ex.getStatus());
            throw GrpcExceptionMapper.map(ex);
        }

        //4 validate stock

        int currentInCart = existingItem.map(CartItemEntity::getQuantity).orElse(0);
        int requestedTotal = currentInCart +request.quantity();

        // 4. VALIDATE: Ensure requested total doesn't exceed warehouse stock
        if (requestedTotal > inventory.getAvailableStock()) {
            log.warn("Stock exceeded for product {}. Available: {}, Requested: {}",
                    request.productId(), inventory.getAvailableStock(), requestedTotal);
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, request.productId().toString());
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(requestedTotal);
        } else {
            CartItemEntity newItem = cartMapper.toEntity(request);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }


        cartRepository.save(cart);
        return getCartByUserId(userId); // Return the full enriched cart
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByUserId(String userId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        // ENRICHMENT LOGIC: Map Entity + gRPC data -> DTO
        Set<CartItemDTO> enrichedItems = cart.getItems().stream()
                .map(this::enrichItem)
                .collect(Collectors.toSet());

        BigDecimal total = enrichedItems.stream()
                .map(CartItemDTO::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return cartMapper.toResponseDTO(userId, enrichedItems, total);
    }

    private CartItemDTO enrichItem(CartItemEntity item) {
        // CALL gRPC: Fetching live product data

        ProductResponse product;
        try {
             product = productClient.getProductMetadata(item.getProductId().toString());

        }catch (StatusRuntimeException ex){
            log.warn("gRPC error during login", ex);
            throw GrpcExceptionMapper.map(ex);
        }
        BigDecimal price = new BigDecimal(product.getPrice());

        return new CartItemDTO(
                item.getProductId(),
                product.getName(),
                product.getImageUrl(),
                item.getQuantity(),
                price,
                price.multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public CartResponseDTO removeItemFromCart(String userId, String productId) {
        // 1. Fetch the cart
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, userId));

        // 2. Remove the item from the collection
        // The orphanRemoval = true in your Entity will trigger the DELETE in SQL
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(UUID.fromString(productId)));

        if (!removed) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, userId);
        }

        // 3. Save the parent (Syncs the state)
        cartRepository.save(cart);

        // 4. Return the updated, enriched cart
        return getCartByUserId(userId);
    }


    private CartEntity findCartOrThrow(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND, userId));
    }


    private @NonNull CartItemEntity findItemInCart(@NonNull CartEntity cart, String productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(UUID.fromString(productId)))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));
    }
}