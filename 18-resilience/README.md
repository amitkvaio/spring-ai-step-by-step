# 18 - Timeouts, retries and rate limits

## What problem are we solving?

A model request can hang or fail temporarily. We need a small, predictable waiting policy rather than unlimited retries.

A help desk waits briefly and may retry a harmless lookup once. It also limits how many requests it handles. Retrying an order or payment would need different rules.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson18/LessonController.java). The key part is:

```java
.retryWhen(Retry.backoff(1,Duration.ofMillis(100)).filter(LessonController::transientFailure)
    .onRetryExhaustedThrow((spec,signal) -> signal.failure()))
.timeout(Duration.ofSeconds(5))
```

The overall five-second deadline includes the one allowed retry. Only transient network, throttling and server failures qualify. Two requests may run at once; a local gate allows ten requests per minute. Completion, failure and cancellation release the concurrency slot.

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **18-resilience**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8098** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "Explain what a college help desk does."
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8098/api/chat -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "A college help desk helps students find the right support."
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson18/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8098/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Warm the local model before class with a short CLI question. Send a short request to this API; a CPU-bound model can still hit the deliberately strict five-second deadline.
2. Show **504** for a deadline and **502** for an upstream failure when those paths occur. They demonstrate the bounded policy, not successful generation.
3. Run the virtual-time tests to show a controlled transient failure retried once and cancellation releasing the slot. The rate-gate test deterministically rejects request eleven; live rate limits use wall-clock minute boundaries.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What is the difference between a timeout and a retry?**

A timeout bounds waiting. A retry makes another attempt after a qualifying failure; both need a shared overall budget.

**Q2. Why retry only selected failures?**

Temporary network, throttling and server failures can recover. Invalid inputs or authorization failures normally need a correction, not repetition.

**Q3. Why avoid blindly retrying side-effecting tools?**

A previous attempt may have applied the action even if its response was lost. Writes need durable idempotency or explicit reconciliation.

**Q4. What is the difference between concurrency and request-rate limits?**

Concurrency limits simultaneous work. A rate limit caps requests over time; this lesson demonstrates both per process.

**Q5. Why release a permit on cancellation too?**

A disconnected or timed-out request must not permanently occupy a slot. Leaking permits can eventually block all later callers.

## A few things to remember

- Timeout returns 504, an upstream failure returns 502, and a local limit returns 429.
- No tools or writes are attached, so this example retries only text generation.
- The limits are per application instance, not a distributed rate limiter. Tests use virtual time to verify deadlines and retries without waiting five seconds.

[Tests](src/test/java/com/example/lesson18/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Temporarily lower the deadline and try a slow local model. Check that failures free the slot for another request.

Next: [MCP server](../19-mcp-server/README.md).

Official reference: [Spring AI - Timeouts, retries and rate limits](https://docs.spring.io/spring-ai/reference/api/chatclient.html). Use the [2.0.2 baseline](../VERSIONS.md).
