# Run the college help desk

Run commands inside `24-college-knowledge-assistant`. Use local demo data only. The included account passwords are public classroom examples, not real credentials.

## Option 1: Everything in Compose

Start Docker Desktop. Then build the JAR and start the services.

PowerShell:

```powershell
.\mvnw.cmd clean verify
docker compose up -d --build
docker compose exec ollama ollama pull llama3.2:3b
docker compose exec ollama ollama pull nomic-embed-text
```

macOS/Linux:

```bash
sh ./mvnw clean verify
docker compose up -d --build
docker compose exec ollama ollama pull llama3.2:3b
docker compose exec ollama ollama pull nomic-embed-text
```

Compose starts PostgreSQL/PGvector, Ollama and the app. Chat and embedding models use local memory and disk space. Model downloads are explicit. The app listens at **http://localhost:8104**; PostgreSQL is mapped to **localhost:5433**.

The default database is `course`, with demo user/password `course/local-course`. Override with `DB_USER` and `DB_PASSWORD` before the first database startup. `JDBC_URL` selects an existing database when running the app outside Compose. Existing volumes retain their original database password.

The index uses **768 dimensions** for `nomic-embed-text`. Changing the embedding model requires a matching schema and reindexing. Do not mix vectors from different models.

## Option 2: Java on the host

Run PostgreSQL alone with `docker compose up -d --wait postgres`. Install/start Ollama on the host and pull both models. Then use the README's wrapper run command. The default URLs already target local Ollama and port 5433.

For a temporary demo without PostgreSQL, use the `memory` profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=memory"
```

```bash
sh ./mvnw spring-boot:run "-Dspring-boot.run.profiles=memory"
```

The memory index disappears on restart. A packaged alternative is `java -jar target/24-college-knowledge-assistant-1.0.0.jar --spring.profiles.active=memory`.

## Approve documents, then ask

Accounts: `alice/local-alice`, `bob/local-bob`, `staff/local-staff`. They are deliberately simple local identities.

First ingest the included handbook as staff:

```powershell
curl.exe -u staff:local-staff -X POST http://localhost:8104/api/admin/index
```

Then ask as Alice:

```powershell
curl.exe -u alice:local-alice http://localhost:8104/api/ask -H "Content-Type: application/json" --data-binary "@request.json"
```

Use `curl` on macOS/Linux. Keep the same `conversation` value for follow-up questions. The server combines it with the authenticated user, so Bob using the same value gets different memory.

Staff can approve custom text with `POST /api/admin/documents`:

```json
{"source":"appointment","owner":"bob","text":"Bob's library appointment is Friday at 10:00."}
```

Owner is `public`, `alice` or `bob`. Source names are limited to letters, digits, underscores and hyphens. Ingesting the same source replaces its chunks and clears old chat memory. Reindexing is delete-then-add; use staged index versions for atomic updates in a real service.

## What each piece adds

| Feature | Why we need it | Earlier lesson |
|---|---|---|
| Ingestion | Prepare approved passages with source IDs | [13](../13-document-ingestion/README.md) |
| Embeddings and PGvector | Find related passages and keep the index | [11](../11-embeddings-semantic-search/README.md), [12](../12-vector-store/README.md) |
| RAG and source validation | Draft from current passages and reject invented source IDs | [14](../14-basic-rag/README.md), [16](../16-evaluation-testing/README.md) |
| Bounded memory | Carry selected earlier turns | [06](../06-chat-memory/README.md) |
| Read-only hours tool | Look up today's demo opening schedule | [08](../08-tool-calling/README.md) |
| Typed, validated answer | Return answer and references as fixed fields | [04](../04-structured-output/README.md) |
| Identity and owner filters | Keep another student's documents out of the prompt | [21](../21-security/README.md) |
| Limits, safe failures and metrics | Bound work and observe outcomes | [17](../17-observability/README.md), [18](../18-resilience/README.md) |

## Check and operate

- Liveness: http://localhost:8104/actuator/health/liveness
- Readiness: http://localhost:8104/actuator/health/readiness
- Latency: `/actuator/metrics/course.knowledge.latency` (login required).
- Clear your memory: `DELETE /api/conversations/classroom-1` with your own login.
- Unsupported questions should return an abstention and no sources. A real model can still make a wrong claim with a valid source ID; review factual support separately.

`docker compose logs --tail 50 app` shows startup failures. `docker compose down` stops services while keeping the vector index and model downloads. Optional `docker compose down -v` discards both volumes. These Compose projects use isolated volumes but the same host database port; stop lesson 12 before starting this database.

Before public deployment, replace demo authentication with your identity provider, add TLS and a distributed rate limit, secure metrics and backups, pin image digests, and run the real PostgreSQL/model checks. Keep database backups in your approved backup store and test restore before relying on them. No cloud deployment or paid call is performed by this repository.
