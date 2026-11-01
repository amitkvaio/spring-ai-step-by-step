# 12 - Persistent vector stores

> **Lesson 12** · **Local port: 8092** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- Our FAQ vectors disappear when the app stops.
- We need to save the index and search it again after a restart.

- Think of a library catalog that stays on disk.
- PGvector stores the text, embedding and source metadata in PostgreSQL.
- A similarity search finds nearby vectors.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson12/LessonController.java).
- The key part is:

```java
SearchRequest.builder().query(q.question()).topK(3)
    .filterExpression("kind == 'faq'").build()
```

- `/api/index` writes two approved FAQs with stable IDs.
- `/api/search` asks for three similar documents, restricted to FAQ metadata.
- `PgVectorStore` handles embedding and database operations.
- This is a vector-search database, not a replacement for your usual MySQL business database.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Also pull the embedding model: `ollama pull nomic-embed-text`.
- Open a terminal inside **12-vector-store**.
- Each project has its own POM and wrapper.

- Start Docker Desktop, then run `docker compose up -d --wait` inside this folder.
- PostgreSQL listens on port 5433.
- The default store uses 768 dimensions for `nomic-embed-text`.
- For an in-memory comparison, append `"-Dspring-boot.run.profiles=memory"` to the run command.

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
sh ./mvnw clean verify
sh ./mvnw spring-boot:run
```

- Wait for `Started LessonApplication`.
- Open **http://localhost:8092** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

First index the supplied FAQs: `curl.exe -X POST http://localhost:8092/api/index`.

[request.json](request.json) contains:

```json
{
  "question": "Library closing time?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8092/api/search -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```json
[
  {
    "id": "6db11ae8-1f5b-42ea-b754-32d09987eb31",
    "text": "The college library closes at 17:00 Monday to Friday.",
    "score": 0.9
  }
]
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson12/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8092/api/search` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- If using the memory profile in the IDE, set `SPRING_PROFILES_ACTIVE=memory` in its run environment.
- Keep the default profile and PostgreSQL running when demonstrating persistence.

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. For the persistence lesson, start the supplied PostgreSQL Compose service and keep the default profile.
   - Pull `nomic-embed-text` before class.
2. In Postman, send **POST** `/api/index` with no body.
   - Then send the search request from `request.json` and show the returned text, ID and score.
3. Stop and restart only the Java app.
   - Search again without indexing; the database-backed result should remain.
   - If using the memory profile, present it as the non-persistent comparison, not proof of database persistence.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. How is vector search different from an ordinary equality lookup?**

- It ranks nearby numeric representations rather than requiring an exact field value.
- Business constraints and metadata filters still apply.

**Q2. What does PGvector add to PostgreSQL?**

- Vector storage and similarity-search operations.
- Spring AI uses those operations through its VectorStore integration.

**Q3. Why must vector dimensions match the embedding model?**

- The database column and search operations expect a fixed vector length.
- Changing the model can require a new schema and reindexing.

**Q4. Why assign stable document IDs?**

Repeated indexing can replace the intended records rather than creating duplicates with fresh random IDs each time.

**Q5. What evidence proves persistence?**

- A search after restarting the application while keeping the database volume.
- A mocked test or a memory-profile search does not establish durable storage.

## A few things to remember

- The Compose database uses public local credentials `course/local-course`; change them through `DB_USER` and `DB_PASSWORD` for your own environment.
- `docker compose down` stops the database but keeps its volume.
- `docker compose down -v` also discards the lesson index.
- Routine tests mock the store.
- Set `LIVE_VECTOR_TESTS=true` before `verify` to opt into the real Ollama + PostgreSQL test.
- It is skipped by default.

- [Tests](src/test/java/com/example/lesson12/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Index, stop and restart the app, then search without indexing again.
- Compare the persistent profile with the memory profile.

Next: [Document ingestion](../13-document-ingestion/README.md).

- Official reference: [Spring AI - Persistent vector stores](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
