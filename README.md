# Document Service 📁

Handles multipart file uploads, securely storing files and mapping metadata to employees.

## 🛠️ Tech Stack
- **Java**: 25
- **Spring Boot**: 4.1.0
- **Database**: MongoDB

## ✨ Architecture Highlights
- **Manual Mongo Override**: Due to a severe Spring Boot 4.1.0 auto-configuration bug that causes connections to stubbornly fall back to the default `test` database, we implemented a manual `MongoConfig.java` to explicitly and aggressively force the `workforce_document_db` database name.
- **Storage Layer**: Built with a scalable local file storage system intentionally structured to allow seamless future migration to Google Cloud Platform (GCP).
- **Security**: A strict `.gitignore` rule guarantees user `uploads/` are never committed to version control.

## 📍 Key Endpoints
- `POST /api/v1/documents/upload` - Upload document (`multipart/form-data`)
- `GET /api/v1/documents/employee/{employeeId}` - List employee's documents
- `GET /api/v1/documents/download/{id}` - Download document (Blob stream)
- `DELETE /api/v1/documents/{id}` - Delete document

## 🚀 Running Locally
- Port: Configured via Config Server.
- Ensure MongoDB is actively running locally on port `27017`.
