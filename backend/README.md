# AI Career Assistant

Production-style learning and career assistant microservices platform.

## Services

- `eureka_server`: service discovery on port `8761`
- `api-gateway`: JWT-secured gateway on port `8080`
- `user-service`: registration and login on port `8081`
- `document-service`: document metadata, file upload, and RabbitMQ event publishing on port `8082`
- `ai-service`: document indexing, grounded Q&A, learning plans, resume reviews, and AI notifications on port `8083`
- `notification-service`: consumes notification events on port `8084`

## Infrastructure

- MySQL stores users and document metadata.
- Redis stores fast session/cache data such as last issued login tokens.
- RabbitMQ moves document upload and notification events asynchronously.
- Qdrant stores document embeddings for semantic retrieval.
- OpenAI creates embeddings and grounded final answers.
- Multi-agent AI workflow runs Research, Planning, Review, and Response agent nodes for RAG answers.
- Docker Compose runs local infrastructure and service containers.
- Kubernetes manifests deploy services, backing infrastructure, and an AI-service HPA.
- GitHub Actions builds every service and Docker image.

## Run locally

```bash
set OPENAI_API_KEY=your_api_key
docker compose up -d
```

Build service jars first if you want to run all app containers through Compose:

```bash
for dir in eureka_server api-gateway user-service document-service ai-service notification-service; do (cd "$dir" && ./gradlew bootJar); done
docker compose up --build
```

For IDE development, start services in this order after `docker compose up -d mysql rabbitmq redis`:

1. `eureka_server`
2. `user-service`
3. `document-service`
4. `ai-service`
5. `notification-service`
6. `api-gateway`

## Smoke test

Register and login through the gateway:

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"name\":\"Pavan\",\"email\":\"pavan@example.com\",\"password\":\"password123\"}"
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"pavan@example.com\",\"password\":\"password123\"}"
```

Upload a document with the returned token:

```bash
curl -X POST http://localhost:8080/api/documents -H "Authorization: Bearer <token>" -F "file=@sample.pdf"
```

Expected result: `document-service` stores the file and metadata, publishes `document.uploaded`, and `ai-service` logs the received event.
The AI service extracts text with Apache Tika, chunks it, creates embeddings, and upserts vectors into Qdrant.

Ask a grounded question:

```bash
curl -X POST http://localhost:8080/api/ai/ask -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"userId\":1,\"question\":\"What is this document about?\"}"
```

Expected result: `ai-service` embeds the question, retrieves top matching chunks from Qdrant, sends the retrieved context to the chat model, and returns an answer with source chunks.

Create a learning plan:

```bash
curl -X POST http://localhost:8080/api/ai/learning-plan -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"goal\":\"Spring Boot microservices\",\"currentLevel\":\"beginner\",\"weeks\":6}"
```

Review resume text:

```bash
curl -X POST http://localhost:8080/api/ai/resume-review -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"targetRole\":\"Java Backend Developer\",\"resumeText\":\"Java Spring Boot project with MySQL and RabbitMQ\"}"
```

Generate interview preparation:

```bash
curl -X POST http://localhost:8080/api/ai/interview-prep -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"targetRole\":\"Java Backend Developer\",\"experienceLevel\":\"junior\",\"focusArea\":\"Spring Boot microservices\"}"
```

## Implementation Notes

- The RAG pipeline is production-style Java/Spring: Tika extraction, chunking, OpenAI embeddings, Qdrant vector retrieval, Redis answer caching, and OpenAI response generation.
- The multi-agent workflow is implemented natively in Spring Boot as separate Research, Planning, Review, and Response agent components.
- It is LangGraph-style orchestration, but not a Python LangChain/LangGraph runtime. Add a Python AI service if you need literal LangChain package usage.

## Kubernetes

```bash
kubectl apply -f k8s/platform.yaml
kubectl apply -f k8s/services.yaml
```

Before deploying to a real cluster, push Docker images to a registry and update image names in `k8s/services.yaml`.
