package com.workforcehub.documentservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {
    @Id
    private String id;
    
    private Long employeeId;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
