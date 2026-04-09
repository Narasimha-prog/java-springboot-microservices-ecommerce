package com.eswar.authenticationservice.grpc.client;

import com.eswar.grpc.user.UserEmailRequest;
import com.eswar.grpc.user.UserIdRequest;
import com.eswar.grpc.user.UserResponse;
import com.eswar.grpc.user.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GrpcUserServiceClient {

    @GrpcClient("user-service")
    private  UserServiceGrpc.UserServiceBlockingStub stub;



    public UserResponse getUserByEmail(String email) {
        UserEmailRequest request = UserEmailRequest.newBuilder()
                .setEmail(email)
                .build();
        return stub.getUserByEmail(request);
    }

    public UserResponse getUserById(String id) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setId(id)
                .build();
        return stub.getUserById(request);
    }
}
