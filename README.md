# Document Service 📁

| | |
|---|---|
| **Student** | L.K.H. Manuth Lakdiw |
| **Student Number** | 241722018 |
| **Batch** | GDSE-72 |
| **GCP Project** | `workforce-hub-cloud` |

## Project Description

Handles multipart file uploads, securely storing file objects and mapping document metadata to employees. Supports uploading, listing, downloading and deleting documents. File metadata is persisted in MongoDB while the actual file objects are stored in a private Google Cloud Storage bucket.

## 🛠️ Technology Stack

- **Java**: 25
- **Spring Boot**: 4.1.0
- **Spring Cloud**: 2025.1.2
- **Spring Data MongoDB**
- **Google Cloud Storage** (`google-cloud-storage`)
- **Netflix Eureka Client**
- **Spring Cloud Config Client**
- **Spring Cloud LoadBalancer**

## ✨ Architecture Highlights

- **Manual Mongo Override**: A manual `MongoConfig.java` explicitly forces the correct database name, working around a Spring Boot 4.1.0 auto-configuration issue that causes fallback to the default `test` database.
- **GCS Object Storage**: File objects are stored in a private Google Cloud Storage bucket. Objects are not publicly accessible; downloads are served through the Document Service API.
- **Upload Constraints**: Supported file types are **PDF** and **PNG**. Maximum file size is **10 MB**.

## 📍 API Endpoints

Base path: `/api/v1/documents`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/documents/upload` | Upload a document (`multipart/form-data`) |
| `GET` | `/api/v1/documents/employee/{employeeId}` | List documents for an employee |
| `GET` | `/api/v1/documents/download/{id}` | Download a document (blob stream) |
| `DELETE` | `/api/v1/documents/{id}` | Delete a document |

### Upload form fields

| Field | Type | Description |
|---|---|---|
| `file` | File | The document file (PDF or PNG, max 10 MB) |
| `employeeId` | Number | The ID of the associated employee |
| `documentType` | String | A label for the document type (e.g. Resume, Contract) |

## 🚀 Running Locally

- **Port**: `8083`
- Ensure MongoDB is running and the Config Server and Eureka Server are available.

```bash
mvn spring-boot:run
```

## ☁️ Production Deployment

- **Runtime**: Regional Managed Instance Group (`workforce-hub-backend-mig`)
- **Region**: `asia-south1`
- **Document Metadata**: MongoDB
- **Document Files**: Google Cloud Storage (private bucket)
- **Process Manager**: PM2 with systemd automatic startup and recovery
