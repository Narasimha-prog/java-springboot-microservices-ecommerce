package com.eswar.userservice.service;

import com.eswar.grpc.user.UserEmailRequest;
import com.eswar.grpc.user.UserIdRequest;
import com.eswar.grpc.user.UserResponse;
import com.eswar.grpc.user.UserServiceGrpc;
import com.eswar.userservice.constants.UserRole;
import com.eswar.userservice.dto.UserGrpcResponse;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.exception.UserNotFoundException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcUserService extends UserServiceGrpc.UserServiceImplBase {

   private final IUserService userService;

    @Override
    public void getUserById(UserIdRequest request, StreamObserver<UserResponse> responseObserver) {

    log.info("grpc request is initialise for getUserById with {}",request.getId());
       UserGrpcResponse userGrpcResponse= userService.getUserByGrpcUserId(UUID.fromString(request.getId()));

        UserResponse response = UserResponse.newBuilder()
                .setId(userGrpcResponse.id().toString())
                .setEmail(userGrpcResponse.email())
                .setPassword(userGrpcResponse.password())
                .setName(userGrpcResponse.firstName()+userGrpcResponse.lastName())
                .addAllRoles(
                        userGrpcResponse.roles().stream()       // Set<UserRole>
                                .map(UserRole::name)      // convert enum to String
                                .toList()                 // to List<String>
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    @Override
    public void getUserByEmail(UserEmailRequest request,
                               StreamObserver<UserResponse> responseObserver) {

        log.info("grpc request is initialise for getUserByEmail with {}",
                 request.getEmail());

        try {

            UserGrpcResponse userGrpcResponse =
                    userService.getUserByGrpcUserEmail(request.getEmail());

            UserResponse response = UserResponse.newBuilder()
                    .setId(userGrpcResponse.id().toString())
                    .setEmail(userGrpcResponse.email())
                    .setPassword(userGrpcResponse.password())
                    .setName(userGrpcResponse.firstName() + userGrpcResponse.lastName())
                    .addAllRoles(
                            userGrpcResponse.roles().stream()
                                    .map(UserRole::name)
                                    .toList()
                    )
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (UserNotFoundException ex) {

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
