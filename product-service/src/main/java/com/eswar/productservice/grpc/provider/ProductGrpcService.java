package com.eswar.productservice.grpc.provider;

import com.eswar.grpc.user.ProductRequest;
import com.eswar.grpc.user.ProductServiceGrpc;
import com.eswar.productservice.exception.BusinessException;
import com.eswar.productservice.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@GrpcService
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

        } catch (IllegalArgumentException ex) {
            log.error("Invalid UUID format: {}", request.getProductId());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Invalid Product ID format")
                    .asRuntimeException());

        } catch (BusinessException businessException) {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("Product not found")
                            .asRuntimeException()
            );

        } catch (Exception ex) {
            log.error("Internal error in getProduct", ex);

            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Internal server error")
                            .asRuntimeException()
            );
        }

    }
}
