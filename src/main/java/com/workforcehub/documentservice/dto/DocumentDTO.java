package com.workforcehub.documentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private String id;
    private Long employeeId;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
