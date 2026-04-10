package com.eswar.orderservice.grpc.client;

import com.eswar.grpc.cart.CartRequest;
import com.eswar.grpc.cart.CartResponse;
import com.eswar.grpc.cart.CartServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class CartServiceClient {


    @GrpcClient("cart-service") // Name of the target service
    private CartServiceGrpc.CartServiceBlockingStub cartStub;

    public CartResponse getCart(String userId) {
        // Create the Request (Using the Fluent Builder Pattern)
        CartRequest request = CartRequest.newBuilder()
                .setUserId(userId)
                .build();

        // MAKE THE CALL! It feels like a local method.
        return cartStub.getCartDetails(request);
    }
}
