package com.eswar.productservice.grpc.provider;

import com.eswar.grpc.user.ProductRequest;
import com.eswar.grpc.user.ProductServiceGrpc;
import com.eswar.productservice.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final IProductService productService;
    @Override
    public void getProduct(ProductRequest request,
                           io.grpc.stub.StreamObserver<com.eswar.grpc.user.ProductResponse> responseObserver) {

        log.info("gRPC request received for productId: {}", request.getProductId());

        try {
            // Call your service layer
            var product = productService.getById(UUID.fromString(request.getProductId()));

            // Build gRPC response
            com.eswar.grpc.user.ProductResponse response =
                    com.eswar.grpc.user.ProductResponse.newBuilder()
                            .setProductId(product.id().toString())
                            .setImageUrl(product.imageUrls().getFirst())
                            .setName(product.name())
                            .setPrice(product.price().doubleValue())
                            .build();

            // Send response
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error fetching product", e);

            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}
