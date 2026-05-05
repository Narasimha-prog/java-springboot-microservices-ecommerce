package com.sitamahalakshmi.review_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    String upload(MultipartFile file, String folder);
}
