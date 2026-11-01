# 15 - Retrieval quality and advanced RAG

## What problem are we solving?

A librarian may miss a useful page because a visitor asks a vague question. We want to improve retrieval before generating an answer.

Try the original query and a clearer rewritten query, then combine their results. `topK` is the result limit; `minScore` is the minimum similarity we accept.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson15/LessonController.java). The key part is:

```java
unique.merge(doc.getId(),doc,(a,b) -> score(a)>=score(b)?a:b);
```

`retrieve` optionally asks the chat model to rewrite the search. It searches both versions, filters for FAQs, removes duplicate IDs, keeps the stronger score, and returns the best results. It deliberately shows retrieval results so you can inspect quality separately from answer wording.

## Run it

Use the [one-time setup](../README.md#set-up-once). Also pull the embedding model: `ollama pull nomic-embed-text`. Open a terminal inside **15-advanced-rag**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8095** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

First index the supplied FAQs: `curl.exe -X POST http://localhost:8095/api/index`.

[request.json](request.json) contains:

```json
{
  "question": "How late can I visit?",
  "topK": 2,
  "minScore": 0.5,
  "rewrite": true
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8095/api/retrieve -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
[
  {
    "id": "6db11ae8-1f5b-42ea-b754-32d09987eb31",
    "text": "The college library closes at 17:00 Monday to Friday.",
    "source": "library",
    "score": 0.9
  }
]
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson15/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8095/api/retrieve` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send **POST** `/api/index` with no body. Submit `request.json` first with `rewrite:false`, then with `rewrite:true`, keeping the other fields fixed.
2. Show the returned source IDs and scores. Explain which extra query is generated and why duplicate document IDs are merged rather than displayed twice.
3. Vary only `minScore` or `topK` and compare against a few hand-labeled expected sources. Count useful matches instead of choosing the version with the longest output.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Why rewrite a retrieval query?**

A short, clearer query can improve matching for vague wording. It adds a model call and can also drift away from the user’s intended question.

**Q2. What does topK control?**

The maximum number of returned candidates. More results can improve recall but also add irrelevant context and increase processing.

**Q3. Is a high similarity threshold always better?**

No. It can exclude useful evidence. Tune it using representative labeled questions rather than treating a score as a universal probability.

**Q4. Why deduplicate by document ID?**

The original and rewritten searches may find the same passage. Combining by stable identity avoids repeated evidence in the result.

**Q5. Is score merging the same as a separate reranker?**

No. This code merges scores from the same embedding space. A reranker is an additional ranking step with its own model or scoring method.

## A few things to remember

- Rewriting adds a model call and may change the intended meaning. Compare `rewrite:false` and `true`.
- Thresholds depend on your model and documents. A high threshold can hide useful results; a low one can include unrelated text.
- This lab does query expansion and score merging. A separate reranking model is an optional extension, not silently included.

[Tests](src/test/java/com/example/lesson15/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Make a small table of questions and expected source names. Compare recall at different thresholds using lesson 16.

Next: [Evaluation and testing](../16-evaluation-testing/README.md).

Official reference: [Spring AI - Retrieval quality and advanced RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html). Use the [2.0.2 baseline](../VERSIONS.md).
