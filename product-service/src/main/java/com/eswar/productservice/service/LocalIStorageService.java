package com.eswar.productservice.service;

import com.eswar.productservice.exception.BusinessException;
import com.eswar.productservice.exception.ErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Profile("dev")
@Service
public class LocalIStorageService implements IStorageService {

    private final Path root = Paths.get("uploads");


    @Override
    public String upload(MultipartFile file, String folder) {
        try {
            // 1. Create the directory if it doesn't exist
            Path resolvePath = root.resolve(folder);
            Files.createDirectories(resolvePath);

            // 2. Create a unique filename to prevent overwriting
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 3. Copy the file bytes to the target location
            Files.copy(file.getInputStream(), resolvePath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            // 4. Return the relative path (This is what goes in the DB)
            return folder + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_CANNOT_SAVED, e.getMessage());
        }
    }
}
