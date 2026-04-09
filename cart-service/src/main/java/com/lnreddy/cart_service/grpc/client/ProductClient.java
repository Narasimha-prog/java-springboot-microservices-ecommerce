package com.lnreddy.cart_service.grpc.client;

import com.eswar.grpc.user.ProductRequest;
import com.eswar.grpc.user.ProductResponse;
import com.eswar.grpc.user.ProductServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class ProductClient {

    private ProductServiceGrpc.ProductServiceBlockingStub productStub;

    public ProductResponse getProductMetadata(String productId) {
        ProductRequest request = ProductRequest.newBuilder().setProductId(productId).build();
        return productStub.getProduct(request);
    }
}
