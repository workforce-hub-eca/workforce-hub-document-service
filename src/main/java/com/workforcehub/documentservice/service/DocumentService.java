package com.workforcehub.documentservice.service;

import com.workforcehub.documentservice.dto.DocumentDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentDTO uploadDocument(Long employeeId, String documentType, MultipartFile file);
    Resource loadDocumentAsResource(String id);
    DocumentDTO getDocumentById(String id);
    List<DocumentDTO> getDocumentsByEmployeeId(Long employeeId);
    void deleteDocument(String id);
    String getDocumentContentType(String id);
}
