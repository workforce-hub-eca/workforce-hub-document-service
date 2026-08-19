package com.workforcehub.documentservice.service.impl;

import com.workforcehub.documentservice.entity.DocumentEntity;
import com.workforcehub.documentservice.exception.ResourceNotFoundException;
import com.workforcehub.documentservice.repository.DocumentRepository;
import com.workforcehub.documentservice.service.DocumentService;
import com.workforcehub.documentservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final RestTemplate restTemplate;

    @Override
    public DocumentEntity uploadDocument(Long employeeId, String documentType, MultipartFile file) {
        validateEmployee(employeeId);

        String fileName = storageService.uploadFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/documents/download/")
                .path(fileName)
                .toUriString();

        DocumentEntity document = new DocumentEntity(
                null,
                employeeId,
                documentType,
                fileName,
                fileDownloadUri,
                LocalDateTime.now()
        );

        return documentRepository.save(document);
    }

    @Override
    public Resource loadDocumentAsResource(String id) {
        DocumentEntity document = getDocumentById(id);
        return storageService.loadFileAsResource(document.getFileName());
    }

    @Override
    public DocumentEntity getDocumentById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    @Override
    public List<DocumentEntity> getDocumentsByEmployeeId(Long employeeId) {
        validateEmployee(employeeId);
        return documentRepository.findByEmployeeId(employeeId);
    }

    @Override
    public void deleteDocument(String id) {
        DocumentEntity document = getDocumentById(id);
        // Note: For a complete implementation, you should also delete the file from the StorageService.
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
}
