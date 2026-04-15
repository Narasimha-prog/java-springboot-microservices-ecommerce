package com.lnreddy.cart_service.grpc.client;

import com.eswar.grpc.inventory.InventoryRequest;
import com.eswar.grpc.inventory.InventoryResponse;
import com.eswar.grpc.inventory.InventoryServiceGrpc;
import com.eswar.grpc.user.ProductRequest;
import com.eswar.grpc.user.ProductResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class InventoryClient {

    @GrpcClient("inventory-service")
   private InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    public InventoryResponse getProductStock(String productId) {
        InventoryRequest request = InventoryRequest.newBuilder().setProductId(productId).build();
        return stub.inventoryDetails(request);
    }
}
