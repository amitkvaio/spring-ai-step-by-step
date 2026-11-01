# 24 - College knowledge assistant

## What problem are we solving?

A college help desk must answer from approved documents, remember a short conversation, and keep each student's private information separate.

Imagine a clerk who checks the handbook, keeps a few notes, and uses a read-only hours lookup. Each part solves a different problem; no prompt gives the clerk extra access.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson24/LessonController.java). The key part is:

```java
.filterExpression("owner == 'public' || owner == '"+owner+"'").build()
```

Staff ingest approved text into PGvector. `KnowledgeService.ask` retrieves only public or owned passages, adds bounded memory, and offers a read-only hours tool. It parses a `Draft`, checks required fields and verifies every returned source ID against the current retrieved documents before returning a typed answer.

## Run it

Use the [one-time setup](../README.md#set-up-once). Also pull the embedding model: `ollama pull nomic-embed-text`. Open a terminal inside **24-college-knowledge-assistant**. Each project has its own POM and wrapper.

Use [DEPLOYMENT.md](DEPLOYMENT.md) for PostgreSQL, Compose, demo accounts and staff ingestion. For a temporary in-memory run, add `"-Dspring-boot.run.profiles=memory"` to the run command. Then index the handbook as staff before asking a question.

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

Wait for `Started LessonApplication`. Open **http://localhost:8104** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "When does the library close?",
  "conversation": "classroom-1"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8104/api/ask -u alice:local-alice -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "The library closes at 17:00.",
  "sources": [
    {
      "id": "<retrieved chunk UUID>",
      "document": "handbook"
    }
  ]
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson24/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8104/api/ask` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | **Basic Auth**: `alice` / `local-alice` |

If using the memory profile in the IDE, set `SPRING_PROFILES_ACTIVE=memory` in its run environment. Keep the default profile and PostgreSQL running when demonstrating persistence.

Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Choose the default PostgreSQL setup for the complete persistence lesson. For a temporary classroom run, set `SPRING_PROFILES_ACTIVE=memory` in the IDE environment; this mode loses its index on restart.
2. As `staff` / `local-staff`, send **POST** `/api/admin/index` with no body. Then ask the existing `request.json` question as `alice` / `local-alice`; show the answer and actual retrieved source IDs.
3. Repeat a follow-up using the same `conversation`. Approve the Bob-only sample from `DEPLOYMENT.md`, then inspect retrieval/context as Alice and Bob. Alice must not receive Bob’s passage; model wording itself is not an authorization test.
4. Ask an unsupported question to illustrate abstention when retrieval finds no evidence. Also show `/actuator/metrics/course.knowledge.latency` with a demo login and the automated unauthorized/citation tests.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. How are users and conversations isolated?**

The memory key combines authenticated user identity and a validated conversation value. Retrieval also filters for public or owned documents before building model context.

**Q2. How are fabricated source references handled?**

Every generated source ID must belong to the current authorized retrieved set. An unknown ID causes a safe 502 response.

**Q3. Does a valid source ID prove the answer is true?**

No. It proves only that the reference points to a permitted retrieved passage. Supporting the actual claim still needs evaluation or review.

**Q4. Why clear history after document approvals change?**

Earlier prompts may contain text that has become stale or is no longer approved. Retaining it could bypass the intended current context boundary.

**Q5. What limits make this a classroom application rather than a production service?**

It serializes turns, uses per-process history and limits, public demo accounts and a non-atomic reindex. Production use needs durable and distributed policies, identity integration, TLS and operational validation.

## A few things to remember

- Blank/oversized input returns 400. There are six stored messages per conversation, 100 active conversations and ten questions per minute per app. The next request clears conversations idle for 30 minutes. Turns are serialized in this teaching example.
- No relevant documents means abstention without a chat call. Unknown citation IDs or unusable model output return 502. Valid IDs still do not prove the answer is factually supported.
- Memory keys include authenticated identity. Ingestion clears old history when approvals change. `/api/admin/**` is staff-only; the model has no write tools.

[Tests](src/test/java/com/example/lesson24/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

As staff, ingest a private Bob document. Ask about it as Alice and Bob. Alice must not receive its content, even if her question tells the model to ignore rules.

Next: [Provider capabilities (optional)](../25-provider-extensions/README.md).

Official reference: [Spring AI - College knowledge assistant](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html). Use the [2.0.2 baseline](../VERSIONS.md).
