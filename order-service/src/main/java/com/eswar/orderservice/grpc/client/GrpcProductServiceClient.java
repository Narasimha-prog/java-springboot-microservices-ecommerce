package com.eswar.orderservice.grpc.client;

import com.eswar.grpc.user.ProductRequest;
import com.eswar.grpc.user.ProductResponse;
import com.eswar.grpc.user.ProductServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class GrpcProductServiceClient {


    @GrpcClient("product-service")
    private ProductServiceGrpc.ProductServiceBlockingStub stub;

    public ProductResponse getProduct(UUID productId) {
        log.info("Calling Product Service via gRPC for productId: {}", productId);

        ProductRequest request = ProductRequest.newBuilder()
                .setProductId(productId.toString())
                .build();

        return stub.getProduct(request);
    }


}
