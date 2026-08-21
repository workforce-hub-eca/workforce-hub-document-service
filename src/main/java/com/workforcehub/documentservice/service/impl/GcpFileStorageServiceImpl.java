package com.workforcehub.documentservice.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.workforcehub.documentservice.exception.ResourceNotFoundException;
import com.workforcehub.documentservice.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GcpFileStorageServiceImpl implements StorageService {

    private final Storage storage;

    @Value("${gcp.bucket.name}")
    private String bucketName;

    public GcpFileStorageServiceImpl() {
        // Automatically picks up GCP credentials from the VM's Service Account
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown-file");
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            
            // Upload the file to GCP Cloud Storage
            storage.create(blobInfo, file.getBytes());

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        Blob blob = storage.get(blobId);

        if (blob == null || !blob.exists()) {
            throw new ResourceNotFoundException("File not found in GCP Bucket: " + fileName);
        }

        byte[] content = blob.getContent();
        return new ByteArrayResource(content);
    }
}
