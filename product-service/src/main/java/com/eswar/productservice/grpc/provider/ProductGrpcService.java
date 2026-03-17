package com.eswar.productservice.grpc.provider;

import com.eswar.grpc.user.ProductServiceGrpc;
import com.eswar.productservice.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final IProductService productService;
  @Override
  public void GetProduct(){

  }
}
