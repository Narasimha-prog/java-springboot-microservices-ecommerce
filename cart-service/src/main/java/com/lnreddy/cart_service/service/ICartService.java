package com.lnreddy.cart_service.service;

import com.lnreddy.cart_service.dto.CartItemRequest;
import com.lnreddy.cart_service.dto.CartResponseDTO;

public interface ICartService {

    // Add or Update quantity of an item
    CartResponseDTO addItemToCart(String userId, CartItemRequest request);

    // Retrieve the full enriched cart
    CartResponseDTO getCartByUserId(String userId);

    // Remove specific item
    CartResponseDTO removeItemFromCart(String userId, String productId);

    // Clear cart after checkout
    void clearCart(String userId);

    CartResponseDTO decrementItemQuantity(String userId, String productId);

    CartResponseDTO incrementItemQuantity(String userId, String productId);
}
