package com.lnreddy.cart_service.service;

import com.lnreddy.cart_service.dto.*;
import com.lnreddy.cart_service.entity.*;
import com.lnreddy.cart_service.mapper.ICartMapper;
import com.lnreddy.cart_service.repository.ICartRepository;
import com.lnreddy.cart_service.grpc.client.ProductClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final ProductClient productClient; // Your gRPC stub wrapper
    private final ICartMapper cartMapper;

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

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.quantity());
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
        var product = productClient.getProductMetadata(item.getProductId().toString());
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
    public CartResponseDTO removeItemFromCart(String userId, String productId) {
        // Implementation for removing a single item logic
        return null;
    }
}