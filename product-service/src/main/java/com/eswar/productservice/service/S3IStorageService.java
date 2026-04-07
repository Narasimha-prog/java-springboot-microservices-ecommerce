package com.eswar.productservice.service;

import com.eswar.productservice.exception.BusinessException;
import com.eswar.productservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class S3IStorageService implements IStorageService {

    private final S3Client s3Client;
    private final String bucketName = "my-ecommerce-bucket";

    @Override
    public String upload(MultipartFile file, String folder) {
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key; // The unique "path" in S3
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_CANNOT_SAVED);
        }
    }
}
