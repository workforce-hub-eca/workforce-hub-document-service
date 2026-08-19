package com.workforcehub.documentservice.service;

import com.workforcehub.documentservice.entity.DocumentEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentEntity uploadDocument(Long employeeId, String documentType, MultipartFile file);
    Resource loadDocumentAsResource(String id);
    DocumentEntity getDocumentById(String id);
    List<DocumentEntity> getDocumentsByEmployeeId(Long employeeId);
    void deleteDocument(String id);
}
