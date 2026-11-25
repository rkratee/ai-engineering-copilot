# AI Engineering Copilot

An AI platform for engineering teams: a knowledge assistant, an automated pull request reviewer, and natural-language analytics, all on one backend.

**Status:** in progress (started Nov 2026)

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

Backend (from the `backend` folder):

```
mvn spring-boot:run
```

Health check: http://localhost:8080/api/health