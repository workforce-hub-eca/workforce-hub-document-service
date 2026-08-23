package com.workforcehub.documentservice.service.impl;

import com.workforcehub.documentservice.dto.DocumentDTO;
import com.workforcehub.documentservice.entity.DocumentEntity;
import com.workforcehub.documentservice.exception.ResourceNotFoundException;
import com.workforcehub.documentservice.repository.DocumentRepository;
import com.workforcehub.documentservice.service.DocumentService;
import com.workforcehub.documentservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final RestTemplate restTemplate;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("application/pdf", "image/jpeg", "image/png");

    @Override
    public DocumentDTO uploadDocument(Long employeeId, String documentType, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid content type. Allowed types are: " + ALLOWED_CONTENT_TYPES);
        }

        validateEmployee(employeeId);

        String objectName = storageService.uploadFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");

        DocumentEntity document = new DocumentEntity(
                null,
                employeeId,
                documentType,
                originalFilename, 
                null, 
                LocalDateTime.now(),
                objectName,
                originalFilename,
                file.getContentType(),
                file.getSize()
        );

        try {
            document = documentRepository.save(document);
        } catch (Exception e) {
            try {
                storageService.deleteFile(objectName);
            } catch (Exception cleanupEx) {
                // Ignore cleanup failure to not mask original exception
            }
            throw e;
        }

        return mapToDTO(document);
    }

    @Override
    public Resource loadDocumentAsResource(String id) {
        DocumentEntity document = getEntityById(id);
        return storageService.loadFileAsResource(document.getStorageObjectName() != null ? document.getStorageObjectName() : document.getFileName());
    }

    @Override
    public String getDocumentContentType(String id) {
        return getEntityById(id).getContentType();
    }

    @Override
    public DocumentDTO getDocumentById(String id) {
        return mapToDTO(getEntityById(id));
    }

    private DocumentEntity getEntityById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    @Override
    public List<DocumentDTO> getDocumentsByEmployeeId(Long employeeId) {
        validateEmployee(employeeId);
        return documentRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(String id) {
        DocumentEntity document = getEntityById(id);
        
        String targetObject = document.getStorageObjectName() != null ? document.getStorageObjectName() : document.getFileName();
        storageService.deleteFile(targetObject);
        
        documentRepository.delete(document);
    }

    private void validateEmployee(Long employeeId) {
        try {
            restTemplate.getForObject("http://employee-service/api/v1/employees/" + employeeId, Object.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        } catch (HttpClientErrorException ex) {
            throw new RuntimeException("Error communicating with employee-service: " + ex.getMessage());
        }
    }

    private DocumentDTO mapToDTO(DocumentEntity entity) {
        return new DocumentDTO(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getDocumentType(),
                entity.getFileName(),
                "/api/v1/documents/download/" + entity.getId(),
                entity.getUploadedAt()
        );
    }
}
