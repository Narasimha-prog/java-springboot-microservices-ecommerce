package com.lnreddy.cart_service.service;

import com.lnreddy.cart_service.dto.*;
import com.lnreddy.cart_service.entity.*;
import com.lnreddy.cart_service.exceptions.BusinessException;
import com.lnreddy.cart_service.exceptions.ErrorCode;
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


    @Transactional
    public CartResponseDTO incrementItemQuantity(String userId, String productId) {
        CartEntity cart = findCartOrThrow(userId);
        CartItemEntity item = findItemInCart(cart, productId);

        // gRPC Check: Verify stock before allowing increment
        var product = productClient.getProductMetadata(productId);
//        if (item.getQuantity() + 1 > product.getStock()) {
//            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, productId);
//        }

        item.setQuantity(item.getQuantity() + 1);
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
    @Transactional
    public CartResponseDTO removeItemFromCart(String userId, String productId) {
        // 1. Fetch the cart
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, userId));

        // 2. Remove the item from the collection
        // The orphanRemoval = true in your Entity will trigger the DELETE in SQL
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

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


    private CartItemEntity findItemInCart(CartEntity cart, String productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));
    }
}