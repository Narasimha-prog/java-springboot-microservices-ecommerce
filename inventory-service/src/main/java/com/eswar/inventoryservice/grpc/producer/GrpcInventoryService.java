package com.eswar.inventoryservice.grpc.producer;


import com.eswar.grpc.inventory.InventoryRequest;
import com.eswar.grpc.inventory.InventoryResponse;
import com.eswar.grpc.inventory.InventoryServiceGrpc;
import com.eswar.inventoryservice.exception.BusinessException;
import com.eswar.inventoryservice.service.IInventoryService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@GrpcService
public class GrpcInventoryService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final IInventoryService inventoryService;

    @Override
    public void inventoryDetails(InventoryRequest request, StreamObserver<InventoryResponse> responseObserver) {

        log.info("grpc request is initialise for inventoryDetails with {}", request.getProductId());


        try {

            var inventoryDto = inventoryService.getInventory(UUID.fromString(request.getProductId()));
            InventoryResponse response = InventoryResponse.newBuilder()
                    .setAvailableStock(inventoryDto.availableQuantity())
                    .build();
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
                            .withDescription("User not found")
                            .asRuntimeException()
            );

        } catch (Exception ex) {
            log.error("Internal error in getUserByEmail", ex);

            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Internal server error")
                            .asRuntimeException()
            );
        }

    }
}
