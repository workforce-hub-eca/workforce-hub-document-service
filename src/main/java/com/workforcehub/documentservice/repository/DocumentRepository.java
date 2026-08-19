package com.workforcehub.documentservice.repository;

import com.workforcehub.documentservice.entity.DocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    List<DocumentEntity> findByEmployeeId(Long employeeId);
}
