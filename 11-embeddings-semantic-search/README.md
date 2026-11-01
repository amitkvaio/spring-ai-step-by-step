# 11 - Embeddings and semantic search

> **Lesson 11** · **Local port: 8091** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- A student asks "Closing time?"
- Our FAQ says "When does the library close?"
- We want to match the meaning, even though the wording differs.

- An embedding represents text as numbers.
- Similar meanings can point in similar directions.
- Cosine similarity compares those directions; it is not a measure of whether an answer is true.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson11/LessonController.java).
- The key part is:

```java
float[] query=embeddings.embed(q.question());
```

- `EmbeddingModel.embed` creates a vector for the question and each FAQ.
- `cosine` scores each pair, and the controller returns the top two matches.
- A chat model is not needed to generate an answer here.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Also pull the embedding model: `ollama pull nomic-embed-text`.
- Open a terminal inside **11-embeddings-semantic-search**.
- Each project has its own POM and wrapper.

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
- Open **http://localhost:8091** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "Closing time?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8091/api/search -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```json
[
  {
    "question": "When does the library close?",
    "score": 0.85
  }
]
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson11/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8091/api/search` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Pull `nomic-embed-text` before class.
   - Send `{"question":"Closing time?"}` and show the ranked FAQ results and similarity scores.
2. Change the wording to `How late can I visit the library?`.
   - Compare the matches, explaining that related meaning need not use the same words.
3. Open `cosine` and its fixed-vector tests.
   - Show a score of one for matching directions and zero for perpendicular directions, separating the arithmetic from real-model quality.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What is an embedding?**

- A numeric vector representing aspects of the input.
- It allows related text to be compared without requiring an exact word match.

**Q2. Is an embedding a reversible copy of the sentence?**

- No; it is not a lossless text encoding or encryption scheme.
- Do not treat the numbers as a way to recover the original sentence exactly.

**Q3. Why use the same embedding model for documents and queries?**

- Their vector spaces must be comparable.
- Equal vector length alone does not make vectors from different models semantically compatible.

**Q4. Does cosine similarity measure factual correctness?**

- No; it measures vector direction similarity.
- A close match can still be irrelevant to the actual task or contain incorrect information.

**Q5. Why store embeddings rather than compute them for every search?**

- Document embeddings usually stay unchanged while many queries arrive.
- Indexing them once avoids repeated computation and is introduced in the next lesson.

## A few things to remember

- Run `ollama pull nomic-embed-text`.
- It is the separate embedding model used from this lesson onwards.
- Use the same embedding model for documents and queries.
- Changing models can change dimensions and meaning.
- Scores shown here are illustrative.
- This small example recalculates the three FAQ vectors per request.
- Lesson 12 stores them.
- Tests use fixed vectors, not learned semantic understanding.

- [Tests](src/test/java/com/example/lesson11/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Ask "How late can I visit?"
- Compare its matches with an exact string search.

Next: [Persistent vector stores](../12-vector-store/README.md).

- Official reference: [Spring AI - Embeddings and semantic search](https://docs.spring.io/spring-ai/reference/api/embeddings/ollama-embeddings.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
