# Architecture

**Status:** accounts and login are implemented. Documents, RAG, PR reviewer, and analytics are planned.

## 1. Requirements

### Functional

**Accounts (done)**
- A user can register with an email and password.
- A user can log in and receive a token valid for 1 hour.
- Only logged-in users can use the API, except register, login, and health.

**Knowledge assistant (planned)**
- A user can upload PDF and Word (.docx) files up to 10 MB each. Other types or larger files are rejected with a clear error.
- A user can ask a question in plain English and gets an answer based only on their own uploaded documents.
- Every answer shows its sources (file name and page).
- If the documents do not contain the answer, the system says so and suggests rephrasing. It never guesses.
- A user can list and delete their own documents, and can never see another user's documents.

**PR reviewer (planned)**
- When a pull request is opened or updated, GitHub sends an event, and the system verifies its signature first.
- The system reviews the changed files and posts comments on the pull request. It skips binary files, huge diffs, and generated files.
- If the team's standards are in the knowledge base, the review uses them.
- If the same event arrives twice, only one review is posted.
- The webhook replies within a few seconds, and the review runs in the background.
- If the AI provider fails, the system retries with a delay. After a few failures it records the review as failed and never blocks the pull request.

**Analytics (planned)**
- An admin can ask questions in plain English about the platform's own data and gets a table or chart.
- Generated queries are read-only, and anything that is not a single SELECT is rejected.
- Every query has a row limit and a time limit, and questions and generated SQL are logged.

### Non-functional
- **Performance (target):** the first words of a chat answer appear within 3 seconds at p95. Normal API calls respond within 300 ms at p95.
- **Cost (target):** average AI cost per question stays under a set limit, and each user has a daily question limit.
- **Scale (assumption):** about 100 users, 1,000 documents, and 50 pull requests a day at launch, with room for 10x without a rewrite.
- **Reliability:** if the AI provider is down, login and document listing still work, and AI features show a clear error. A failed review never loses the event.
- **Security:** passwords are stored only as BCrypt hashes, secrets are never in Git, and one user's data is never visible to another.
- **Privacy:** document text goes only to the configured AI provider. API keys and document contents are never logged.
- **Quality:** answer quality is measured with an evaluation set of 30-50 questions, and a change must not lower the score.

## 2. API list

| Method | Path | Auth | Purpose | Status |
|---|---|---|---|---|
| GET | /api/health | Public | Liveness check | Done |
| POST | /api/auth/register | Public | Create an account | Done |
| POST | /api/auth/login | Public | Returns a JWT | Done |
| GET | /api/me | Logged in | Current user's id and email | Done |
| POST | /api/documents | Logged in | Upload a PDF or .docx | Planned |
| GET | /api/documents | Logged in | List my documents | Planned |
| DELETE | /api/documents/{id} | Logged in (owner) | Delete my document and its chunks | Planned |
| POST | /api/chat | Logged in | Ask a question, streamed answer with sources | Planned |
| POST | /api/webhooks/github | Signature only | Receive pull request events | Planned |
| GET | /api/reviews | Logged in | List reviews and their status | Planned |
| POST | /api/analytics/ask | Admin | Natural-language question, returns rows | Planned |

## 3. Data model

| Table | Main columns | Notes |
|---|---|---|
| users | id, email (unique), password_hash, role, created_at | `role` (USER or ADMIN) is planned |
| documents | id (UUID), owner_id, file_name, content_type, size_bytes, status, created_at | status: UPLOADED, PROCESSING, READY, FAILED |
| document_chunks | id, document_id, chunk_index, page, content, embedding (vector) | Vector size depends on the embedding model chosen |
| questions | id, user_id, question, answer, sources, tokens_in, tokens_out, cost_usd, latency_ms, created_at | Feeds the analytics module |
| repositories | id, github_repo_id, full_name, installation_id | Connected GitHub repositories |
| pr_reviews | id, repository_id, pr_number, head_sha, delivery_id (unique), status, findings_count, cost_usd, created_at, completed_at | Unique delivery_id prevents duplicate reviews |
| analytics_queries | id, user_id, question, generated_sql, status, row_count, duration_ms, created_at | Audit log of generated SQL |

**Relations:** a user has many documents and questions. A document has many chunks. A repository has many reviews.
**Indexes:** unique on `users.email` and `pr_reviews.delivery_id`, an index on `document_chunks.document_id`, and a vector index (HNSW) on `embedding` for fast similarity search.
**Access rule:** every document and question query filters by the logged-in user's id.

## 4. Main flows

### Document upload and ingestion
1. The user uploads a file. The API checks the type and size, stores the file, creates a `documents` row (UPLOADED), and returns immediately.
2. A background worker extracts text, splits it into chunks, and creates an embedding for each chunk.
3. The worker saves the chunks and embeddings, then sets the document to READY (or FAILED with a reason).

### Asking a question (RAG)
1. The API verifies the token and creates an embedding of the question.
2. It searches `document_chunks` for the closest chunks, filtered to the user's own documents.
3. If nothing is close enough, it answers that the documents do not cover the question.
4. Otherwise it builds a prompt from the chunks and calls the LLM, streaming tokens back to the browser.
5. It saves the question, answer, sources, tokens, cost, and latency in `questions`.

### PR review
1. GitHub sends an event. The API verifies the signature, saves a `pr_reviews` row (QUEUED) using `delivery_id` for deduplication, puts a job on the queue, and returns 2xx quickly.
2. A worker fetches the diff, filters out files that should be skipped, retrieves matching standards from the knowledge base, and calls the LLM.
3. The worker posts comments on the pull request and marks the review DONE. On failure, it retries with a delay and then marks the review FAILED.

### Analytics question
1. The admin asks a question. The LLM generates a SQL query against allowed views only.
2. The validator accepts a single SELECT and rejects everything else. The query runs as a read-only database user with a timeout and a row limit.
3. The result goes back as rows, the React app draws a chart, and the question and SQL are logged.

## 5. Estimates and bottlenecks

**Assumptions:** 1,000 documents of about 20 pages each, with chunks of about 500 tokens.
- Text size is about 1,000 × 20 × 500 words, which is roughly 10 million words, or about 13 million tokens.
- That gives about 26,000 chunks. At 1,536 numbers of 4 bytes each, one embedding is about 6 KB, so the vectors take about 160 MB. A single Postgres instance handles that easily.
- Traffic: 100 users asking about 20 questions a day is about 2,000 questions a day, well under 1 request per second on average.

**Likely bottlenecks, in order**
1. LLM latency and rate limits. Mitigation: streaming, timeouts, retries, per-user limits, and caching repeated questions.
2. Embedding bulk uploads. Mitigation: a queue and background workers.
3. Vector search at larger sizes. At 100x (about 2.6 million chunks, roughly 16 GB of vectors) we would need a bigger instance, a tuned vector index, or a dedicated vector database.
4. BCrypt uses CPU on login bursts. Mitigation: rate limiting on login.
5. A single backend instance. Mitigation: it is stateless (JWT), so we can add instances behind a load balancer.

## 6. Key decisions and trade-offs

| Decision | Why | Trade-off |
|---|---|---|
| Modular monolith | Simple to build, test, and deploy for one team | Modules scale together, so it needs discipline to keep boundaries |
| PostgreSQL with pgvector | One datastore, transactional consistency, less to operate | Less specialised than a dedicated vector database at very large scale |
| JWT (stateless) | Any instance can serve any request, so scaling out is easy | A token cannot be revoked before it expires without extra state |
| HS256 with one secret | Simple for one service | With several services, use RS256 so only one can issue tokens |
| Queue for ingestion and reviews | Fast responses, retries, and safe handling of slow AI calls | More moving parts and the need for idempotent workers |
| Refuse when sources are missing | Trust over helpfulness | Some questions get "I don't know" even when the model could guess |
| Read-only DB user and SELECT validation for analytics | Defence in depth against prompt injection | Some valid queries may be rejected |