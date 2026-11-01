# 14 - Basic RAG

## What problem are we solving?

The college help desk needs to answer from approved library information instead of the model's general knowledge.

A clerk checks the handbook before answering. Retrieval-Augmented Generation (RAG) does the same: retrieve relevant passages, then include them in the model request.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson14/LessonController.java). The key part is:

```java
if(docs.isEmpty()) return new Answer("I do not know from the approved documents.",List.of());
```

`ask` searches the vector store first. It sends the retrieved text with the question and asks the model to cite source names. If nothing clears the similarity threshold, it returns "I do not know" without calling the chat model.

## Run it

Use the [one-time setup](../README.md#set-up-once). Also pull the embedding model: `ollama pull nomic-embed-text`. Open a terminal inside **14-basic-rag**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8094** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

First index the supplied FAQs: `curl.exe -X POST http://localhost:8094/api/index`.

[request.json](request.json) contains:

```json
{
  "question": "When does the library close?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8094/api/ask -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "The library closes at 17:00 [library].",
  "sources": [
    "library"
  ]
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson14/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8094/api/ask` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send **POST** `/api/index` with no body after each application restart. Then ask the library-closing question using `request.json`.
2. Display the answer and its `sources` list, and open the corresponding approved passage in the controller. Explain that sources identify retrieved candidates, not automatic proof of every claim.
3. Ask for an admission deadline absent from the index. If no passage meets the threshold, show the abstention and no model invocation; retrieval quality depends on the actual embedding model.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What problem does RAG solve?**

It supplies retrieved application knowledge before generation. The model can answer using information that was not available through its general learned patterns.

**Q2. Does RAG retrain or fine-tune the model?**

No. This application adds selected passages to the current prompt. Its underlying model weights are unchanged.

**Q3. Why abstain when retrieval returns no matches?**

There is no approved context for this application to ground its answer. Calling the model anyway would encourage unsupported guessing.

**Q4. Does a nonempty sources list prove the answer is supported?**

No. The model may still misread or ignore a passage. Check factual support and source relevance separately.

**Q5. What should be evaluated separately in a RAG system?**

Whether retrieval finds the right evidence, and whether generation answers faithfully from that evidence. A polished answer can hide poor retrieval.

## A few things to remember

- Run `/api/index` once after startup. This lesson keeps its two approved passages in memory.
- The `sources` field lists retrieved candidates. Their presence alone does not prove every generated claim is supported. Review the passages.
- There is no conversation history in this lesson. Tests cover the retrieved context and the no-match path.

[Tests](src/test/java/com/example/lesson14/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Ask for the current admission deadline, which is absent from the documents. Check that the assistant abstains rather than inventing a date.

Next: [Retrieval quality and advanced RAG](../15-advanced-rag/README.md).

Official reference: [Spring AI - Basic RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html). Use the [2.0.2 baseline](../VERSIONS.md).
