# 13 - Document ingestion

## What problem are we solving?

The librarian has a handbook, not a list of FAQs. We need to turn it into passages that can be searched.

Ingestion means preparing documents for use. Read the handbook, split it into smaller pieces, keep where each piece came from, and put those pieces in the index.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson13/LessonController.java). The key part is:

```java
store.delete("source == 'handbook'"); store.add(chunks);
```

`HandbookIndexer.chunks` uses `TextReader` and `TokenTextSplitter` on the included `college-handbook.txt`. Each chunk gets a stable ID, its source name and a chunk number. Reindexing removes the old source first, so shorter replacements do not leave stale chunks behind.

## Run it

Use the [one-time setup](../README.md#set-up-once). Also pull the embedding model: `ollama pull nomic-embed-text`. Open a terminal inside **13-document-ingestion**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8093** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8093/api/index -X POST
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response:

```json
{
  "chunks": 6
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson13/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8093/api/index` |
| Body | None |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Open **GET** `http://localhost:8093/api/chunks` in a browser or Postman. Show the text, source and chunk number before making any embedding call.
2. Send **POST** `/api/index` with no body after Ollama is ready. Show the chunk count. Point to `TextReader`, `TokenTextSplitter` and `VectorStore` as the three preparation steps.
3. Add a short paragraph to the included handbook before the lesson, restart/reload the resource as needed, and index again. Compare chunk IDs and source metadata; do not claim this memory index survives application restart.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Why split a document into chunks?**

Retrieval and prompts work better with focused passages than an entire large handbook. Chunk size affects context, relevance and cost.

**Q2. Why keep source metadata on each chunk?**

It connects a retrieved passage to its document and helps filtering, replacement and references in the final answer.

**Q3. Are a chunk number and a page number the same?**

No. This text lesson records chunk positions, not physical document pages. Preserve real page metadata when the reader provides it.

**Q4. Why remove old chunks when replacing a source?**

A shorter replacement may no longer produce the old final chunks. Leaving them behind would keep stale information searchable.

**Q5. Is delete-then-add an atomic reindex?**

No. An embedding or write failure can leave the source absent. Production updates can stage a new index version and switch only after successful preparation.

## A few things to remember

- `GET /api/chunks` shows the prepared passages without needing Ollama. The number of chunks can vary with splitter settings.
- This lesson uses an in-memory vector store so you can focus on ingestion. Reindex after restart. The splitter's tokenizer does not exactly count every Ollama model's tokens.
- Delete-then-add is simple, not an atomic production update. A failed embedding call can leave a source unindexed; production indexing needs staging or version switching.

[Tests](src/test/java/com/example/lesson13/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Add a paragraph to the handbook, index again, and inspect its source and chunk metadata.

Next: [Basic RAG](../14-basic-rag/README.md).

Official reference: [Spring AI - Document ingestion](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html). Use the [2.0.2 baseline](../VERSIONS.md).
