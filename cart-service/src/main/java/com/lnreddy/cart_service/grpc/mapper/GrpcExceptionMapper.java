package com.lnreddy.cart_service.grpc.mapper;

import com.lnreddy.cart_service.exceptions.BusinessException;
import com.lnreddy.cart_service.exceptions.ErrorCode;
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
                    new BusinessException(ErrorCode.SERVICE_UNAVAILABLE);

            case DEADLINE_EXCEEDED ->
                    new BusinessException(ErrorCode.TIMEOUT);

            default ->
                    new BusinessException(ErrorCode.DOWNSTREAM_SERVICE_ERROR);
        };
    }
}