package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        try {

            String originalFileName = file.getOriginalFilename();


            String randomId = UUID.randomUUID().toString();
            String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
            String filePath = path + File.separator + fileName;


            File folder = new File(path);
            if (!folder.exists())
                folder.mkdir();

            Files.copy(file.getInputStream(), Paths.get(filePath));

            log.info("Image saved successfully on this path , {}", filePath);
            return fileName;

        } catch (Exception e) {

            log.error("Failed to update images , {}", e.getMessage());
            throw new APIException("Failed to save Image ");

        }
    }
}
