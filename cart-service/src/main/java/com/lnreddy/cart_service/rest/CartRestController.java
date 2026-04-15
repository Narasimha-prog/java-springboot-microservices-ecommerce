package com.lnreddy.cart_service.rest;

import com.lnreddy.cart_service.dto.CartItemRequest;
import com.lnreddy.cart_service.dto.CartResponseDTO;
import com.lnreddy.cart_service.service.ICartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "CartService Service",description = "to create cart items in ecommerce")
@PreAuthorize("hasRole('USER')") // Guarding the whole class
public class CartRestController {

    private final ICartService cartService;

    @GetMapping()
    public ResponseEntity<CartResponseDTO> getCart(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CartItemRequest request) {
        // Returns 201 Created along with the updated cart
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItemToCart(userId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @AuthenticationPrincipal  String userId,
            @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userId, productId));
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal  String userId) {
        cartService.clearCart(userId);
    }

    @PatchMapping("/items/{productId}/increment")
    public ResponseEntity<CartResponseDTO> incrementItem(
            @AuthenticationPrincipal String userId,
            @PathVariable String productId) {
        // Calls the service logic that performs the gRPC inventory check
        return ResponseEntity.ok(cartService.incrementItemQuantity(userId, productId));
    }

    @PatchMapping("/items/{productId}/decrement")
    public ResponseEntity<CartResponseDTO> decrementItem(
            @AuthenticationPrincipal String userId,
            @PathVariable String productId) {
        // Calls the service logic that handles local quantity reduction
        return ResponseEntity.ok(cartService.decrementItemQuantity(userId, productId));
    }
}