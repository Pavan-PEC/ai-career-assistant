# Low-Level Design (LLD)

## Modules & Key Files
- backend/api-gateway/: routing, authentication filters, rate limiting
- backend/ai-service/: prompts, model adapters, request queueing, retry logic
  - Dockerfile, build.gradle
- backend/document-service/: resume parsing, storage adapters, file validation
- backend/user-service/: user model, JWT handling, password hashing

## APIs (examples)
- POST /api/v1/auth/login -> returns JWT
- POST /api/v1/ai/query -> { prompt, userId } -> returns ai response id
- GET /api/v1/documents/{id} -> returns metadata and signed URL

## Data models (simplified)
- User { id, name, email, password_hash, created_at }
- Document { id, user_id, type, storage_path, parsed_text, created_at }
- Conversation { id, user_id, messages[], ai_response_summary }

## Error handling & observability
- Centralized error handler with structured logs (JSON)
- Expose metrics (Prometheus) and health endpoints (/actuator/health)
- Use correlation IDs for tracing requests across services

## Local dev notes
- Start MySQL and Kafka (if used) via docker-compose
- Load initial DB schema: backend/mysql-init/01-create-databases.sql

## Testing
- Unit tests for services with mocked external model client
- Integration tests in CI that spin up required containers

## Files to show during interviews
- backend/ai-service/src/main/java/.../Adapter.java (explain prompt handling)
- backend/document-service parsing utilities
