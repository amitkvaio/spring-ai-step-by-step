# 17 - Observability, usage and cost

## What problem are we solving?

A help desk operator needs to know how long replies take, how often they fail, and how much model work was used.

A desk records waiting time and work completed. Metrics are those measurements for our application. Token usage comes from provider response metadata, not from counting Java characters.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson17/LessonController.java). The key part is:

```java
var response=client.prompt().user(q.question()).call().chatResponse();
```

`chatResponse()` keeps the reply and usage metadata. `MeterRegistry` records input/output token counts and a latency timer tagged only with success or error. The response estimates cost from configured per-million-token rates; local Ollama defaults those rates to zero.

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **17-observability**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8097** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "Explain what a help desk does."
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8097/api/chat -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "It helps students find information.",
  "inputTokens": 12,
  "outputTokens": 5,
  "estimatedUsd": 0
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson17/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8097/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send a short question, then open **GET** `http://localhost:8097/actuator/metrics/course.chat.latency` in a browser or Postman to show the recorded timer.
2. If the provider reports usage, also inspect **GET** `/actuator/metrics/course.tokens`. Compare the response’s input/output counts with a longer question.
3. Explain that default local pricing rates are zero and missing usage stays null. The displayed estimate is not a provider bill, and token counts must not be inferred from character counts.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Why use chatResponse() instead of content() for observability?**

It preserves metadata such as token usage alongside the generated output. content() extracts only the text.

**Q2. Why keep user IDs and prompts out of metric tags?**

They create high-cardinality series and can expose sensitive data. Use small outcome labels for metrics and controlled tracing where appropriate.

**Q3. Does missing token usage mean generation was free?**

No. It means the provider did not supply usable counts. This example returns unknown values rather than inventing a zero bill.

**Q4. Why record latency in a finally block?**

Failures also consume time. The timer must be stopped even if generation throws an exception.

**Q5. How is the cost estimate calculated?**

Multiply input and output counts by their configured per-million-token rates. Real billing can have additional rules, so the result remains an estimate.

## A few things to remember

- Check `/actuator/metrics/course.chat.latency` and `/actuator/metrics/course.tokens`. Spring AI's own observations are enabled through Actuator too.
- Missing or empty usage is returned as null, not silently treated as free generation.
- Set `INPUT_USD_PER_MILLION` and `OUTPUT_USD_PER_MILLION` for a pricing experiment. This is an estimate, not a bill. Prompts and user IDs are not metric tags.

[Tests](src/test/java/com/example/lesson17/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Compare a short and long prompt. Inspect the reported tokens and latency; avoid assuming every provider reports identical metadata.

Next: [Timeouts, retries and rate limits](../18-resilience/README.md).

Official reference: [Spring AI - Observability, usage and cost](https://docs.spring.io/spring-ai/reference/observability/index.html). Use the [2.0.2 baseline](../VERSIONS.md).
