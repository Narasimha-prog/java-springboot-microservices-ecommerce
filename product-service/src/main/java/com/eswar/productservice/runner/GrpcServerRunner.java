package com.eswar.productservice.runner;


import com.eswar.productservice.grpc.provider.ProductGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "grpc.manual",
        havingValue = "true",
        matchIfMissing = false
        // 👈 This explicitly says: "If the property is missing, don't run this"
)
public class GrpcServerRunner implements ApplicationRunner {


private final ProductGrpcService productGrpcService;

 private  Server server=null;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int port = args.containsOption("grpc.port") ?
                Integer.parseInt(Objects.requireNonNull(args.getOptionValues("grpc.port")).getFirst()) : 9091;

    server   =  ServerBuilder.forPort(port)
                .addService(productGrpcService)
                .build()
                .start();

        log.info("gRPC server started on port {}" , port);
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (server != null) {
            log.info("Shutting down gRPC server...");
            server.shutdown();
            log.info("gRPC server stopped");
        }
    }
}
