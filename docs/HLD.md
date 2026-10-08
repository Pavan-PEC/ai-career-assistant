# High-Level Design (HLD)

## Overview
This project is an AI Career Assistant composed of a frontend SPA and multiple backend microservices. The goal is to provide intelligent career guidance, resume help, interview prep, and job search assistance.

## Components
- Frontend (frontend/): React + Vite SPA serving the UI and demo flows.
- API Gateway (backend/api-gateway): Routes requests to services, handles auth.
- AI Service (backend/ai-service): Encapsulates model interactions and prompts.
- Document Service (backend/document-service): Stores user documents and resumes.
- User Service (backend/user-service): User profiles, authentication.
- Eureka/Discovery and Config services for service discovery and central config.

## Interactions
1. User interacts with SPA -> API Gateway.
2. Gateway forwards to appropriate microservice.
3. AI Service calls model providers (external) and stores conversation logs.
4. Document service manages uploads and parsing (resume parsing).

## Deployment
- Docker Compose for local development (backend/docker-compose.yml).
- Kubernetes manifests in backend/k8s for production-like deployment.
- CI pipeline: backend/.github/workflows/ci.yml

## Tech stack
- Frontend: React, Vite
- Backend: Spring Boot (Gradle)
- DB: MySQL (docker)
- Infrastructure: Docker, Kubernetes

## Security & Privacy
- No secrets in repo. Use env vars and secret stores for credentials.
- Validate and sanitize uploaded documents.

## For Interviews
Use this HLD to explain component choices, trade-offs, and how the AI Service is isolated for safer upgrades.