package com.eswar.userservice.grpc.provider;

import com.eswar.grpc.user.UserEmailRequest;
import com.eswar.grpc.user.UserIdRequest;
import com.eswar.grpc.user.UserResponse;
import com.eswar.grpc.user.UserServiceGrpc;
import com.eswar.userservice.constants.UserRole;
import com.eswar.userservice.dto.UserGrpcResponse;
import com.eswar.userservice.exception.BusinessException;
import com.eswar.userservice.service.IUserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@GrpcService
public class GrpcUserService extends UserServiceGrpc.UserServiceImplBase {

   private final IUserService userService;

    @Override
    public void getUserById(UserIdRequest request, StreamObserver<UserResponse> responseObserver) {

    log.info("grpc request is initialise for getUserById with {}",request.getId());

    try {
        UserGrpcResponse userGrpcResponse = userService.getUserByGrpcUserId(UUID.fromString(request.getId()));

        UserResponse response = UserResponse.newBuilder()
                .setId(userGrpcResponse.id().toString())
                .setEmail(userGrpcResponse.email())
                .setPassword(userGrpcResponse.password())
                .setName(userGrpcResponse.firstName() + userGrpcResponse.lastName())
                .addAllRoles(
                        userGrpcResponse.roles().stream()       // Set<UserRole>
                                .map(UserRole::name)      // convert enum to String
                                .toList()                 // to List<String>
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    } catch (IllegalArgumentException ex) {
        log.error("Invalid UUID format: {}", request.getId());
        responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Invalid User ID format")
                .asRuntimeException());

    }
    catch (BusinessException businessException){
        responseObserver.onError(
                io.grpc.Status.NOT_FOUND
                        .withDescription("User not found")
                        .asRuntimeException()
        );

    }catch(Exception ex){
        log.error("Internal error in getUserByEmail", ex);

        responseObserver.onError(
                io.grpc.Status.INTERNAL
                        .withDescription("Internal server error")
                        .asRuntimeException()
        );
    }
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

        } catch (BusinessException ex) {

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
