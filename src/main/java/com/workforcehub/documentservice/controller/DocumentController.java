package com.workforcehub.documentservice.controller;

import com.workforcehub.documentservice.dto.DocumentDTO;
import com.workforcehub.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {
        DocumentDTO document = documentService.uploadDocument(employeeId, documentType, file);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByEmployeeId(@PathVariable Long employeeId) {
        List<DocumentDTO> documents = documentService.getDocumentsByEmployeeId(employeeId);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String id, HttpServletRequest request) {
        Resource resource = documentService.loadDocumentAsResource(id);
        String contentType = documentService.getDocumentContentType(id);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String filename = resource.getFilename() != null ? resource.getFilename() : "document";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);
        headers.add("X-Content-Type-Options", "nosniff");
        try {
            headers.setContentLength(resource.contentLength());
        } catch (IOException e) {
            // content length unknown
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
