# AI Engineering Copilot

An AI platform for engineering teams: a knowledge assistant, an automated pull request reviewer, and natural-language analytics, all on one backend.

**Status:** in progress (started Nov 2026)

See [docs/architecture.md](docs/architecture.md) for the design and trade-offs.

## Problem

Engineering teams lose time in three ways:
- Knowledge is scattered across docs and wikis, so the same questions get asked again and again.
- Pull requests wait for reviews, and reviewers miss issues.
- Nobody can easily see how code reviews and questions are going without writing SQL or building dashboards.

## Solution

One web platform with three modules:

1. **Knowledge assistant:** upload team documents, ask questions in plain English, and get answers with source citations. If the documents don't support an answer, it says so.
2. **PR reviewer:** when a pull request opens on GitHub, the system reviews the changes using the team's standards and posts comments.
3. **Analytics:** ask questions about the platform's own data (review findings, usage, AI cost) and get charts back, using safe read-only queries.

## Tech stack

- Frontend: React, TypeScript
- Backend: Java 21, Spring Boot
- Database: PostgreSQL with pgvector
- Cache and queue: Redis
- Infrastructure: Docker, GitHub Actions, AWS

## Roadmap

- [x] Repository and Spring Boot skeleton
- [ ] Database, user login (JWT)
- [ ] Knowledge assistant (RAG, streaming, citations, evals)
- [ ] First deployment
- [ ] PR reviewer
- [ ] Analytics module
- [ ] Architecture diagram, decision log, demo video

## Run locally

**Prerequisites:** Java 17+ (project targets the version in `pom.xml`), Node.js (LTS), PostgreSQL 18.

**1. Database:** create a database named `copilot`.

**2. Environment variables** (never commit these):
- `DB_PASSWORD`: your PostgreSQL password
- `JWT_SECRET`: a random secret of at least 32 bytes

**3. Backend** (from the `backend` folder):
```
.\mvnw spring-boot:run
```
Health check: http://localhost:8080/api/health

**4. Frontend** (from the `frontend` folder, in a second terminal):
```
npm install
npm run dev
```
Open http://localhost:5173. The dev server forwards `/api` calls to the backend on port 8080.