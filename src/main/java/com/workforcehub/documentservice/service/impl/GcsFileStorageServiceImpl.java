package com.workforcehub.documentservice.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.workforcehub.documentservice.exception.ResourceNotFoundException;
import com.workforcehub.documentservice.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "document.storage.provider", havingValue = "gcs")
public class GcsFileStorageServiceImpl implements StorageService {

    private final Storage storage;
    private final String bucketName;

    public GcsFileStorageServiceImpl(Storage storage, @Value("${document.storage.gcs.bucket}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        String objectName = "documents/" + UUID.randomUUID().toString();

        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("originalFilename", originalFilename);
            metadata.put("contentType", file.getContentType());
            
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setMetadata(metadata)
                    .build();

            storage.create(blobInfo, file.getBytes(), Storage.BlobTargetOption.doesNotExist());
            
            return objectName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not read file for GCS upload", ex);
        } catch (StorageException ex) {
            throw new RuntimeException("GCS upload failed", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String objectName) {
        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);
        if (blob == null || !blob.exists()) {
            throw new ResourceNotFoundException("File not found in GCS: " + objectName);
        }
        
        return new InputStreamResource(Channels.newInputStream(blob.reader())) {
            @Override
            public String getFilename() {
                if (blob.getMetadata() != null && blob.getMetadata().containsKey("originalFilename")) {
                    return blob.getMetadata().get("originalFilename");
                }
                return objectName;
            }

            @Override
            public long contentLength() {
                return blob.getSize();
            }
        };
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            storage.delete(blobId); 
        } catch (StorageException ex) {
            throw new RuntimeException("GCS delete failed", ex);
        }
    }
}
