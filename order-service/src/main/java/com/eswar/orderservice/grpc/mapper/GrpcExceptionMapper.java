package com.eswar.orderservice.grpc.mapper;

import com.eswar.orderservice.exceptions.BusinessException;
import com.eswar.orderservice.exceptions.ErrorCode;
import com.eswar.orderservice.exceptions.BusinessException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class GrpcExceptionMapper {


    private GrpcExceptionMapper(){}
    public static BusinessException map(StatusRuntimeException ex) {

        Status.Code code = ex.getStatus().getCode();

        return switch (code) {

            case NOT_FOUND ->
                    new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);

            case UNAVAILABLE ->
                    new BusinessException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);

            case DEADLINE_EXCEEDED ->
                    new BusinessException(ErrorCode.TIMEOUT);

            default ->
                    new BusinessException(ErrorCode.DOWNSTREAM_SERVICE_ERROR);
        };
    }
}