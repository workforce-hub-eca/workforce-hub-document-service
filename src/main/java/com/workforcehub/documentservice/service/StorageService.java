package com.workforcehub.documentservice.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file);
    Resource loadFileAsResource(String objectName);
    void deleteFile(String objectName);
}
