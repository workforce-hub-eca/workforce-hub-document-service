package com.workforcehub.documentservice;

import com.workforcehub.documentservice.repository.DocumentRepository;
import com.workforcehub.documentservice.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(args = "--spring.config.name=application-test")
@ImportAutoConfiguration(exclude = {
    org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration.class,
    org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration.class,
    org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration.class
})
class DocumentServiceApplicationTests {

    @MockitoBean
    private DocumentRepository documentRepository;

    @MockitoBean
    private StorageService storageService;

    @Test
    void contextLoads() {
    }
}
