# 23 - Production operations

> **Lesson 23** · **Local port: 8103** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

An application that works in a terminal still needs a repeatable start, health checks and a clean shutdown.

- Before opening a help desk, check that the desk is alive and that its supporting service can be reached.
- Liveness and readiness answer those different questions.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson23/LessonController.java).
- The key part is:

```java
client.prompt().user(q.question()).call().content()
```

- The Dockerfile runs the packaged JAR as a non-root user.
- Compose starts the app and Ollama with a persistent model volume.
- Actuator exposes health checks.
- HTTP clients have three-second connect and thirty-second read timeouts, and shutdown gives active requests time to finish.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **23-production**.
- Each project has its own POM and wrapper.

- For container startup, model pulls, readiness checks and cleanup, follow [DEPLOYMENT.md](DEPLOYMENT.md).
- The local wrapper run below still works without Docker.

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
- Open **http://localhost:8103** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "What does a college help desk do?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8103/api/chat -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "It helps students find information and support."
}
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson23/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8103/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. For a host demonstration, run from the IDE or Maven with local Ollama.
   - For the container lesson, use the existing `DEPLOYMENT.md` commands after starting Docker Desktop.
2. Open the liveness and readiness URLs in two browser tabs: `/actuator/health/liveness` and `/actuator/health/readiness`.
   - Show the JSON statuses before a chat request.
3. Stop Ollama after the successful request and refresh both tabs.
   - Liveness should remain UP while readiness becomes DOWN.
   - Restart Ollama and check recovery.
   - Explain that this readiness check verifies service reachability, not model installation or answer quality.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What is the difference between liveness and readiness?**

- Liveness asks whether the application is alive.
- Readiness asks whether it should receive traffic under its configured dependency checks.

**Q2. What does this readiness probe actually check?**

- That Ollama’s tags endpoint responds successfully, plus the application readiness state.
- It does not run generation or verify a particular model is loaded.

**Q3. Why run a container as a non-root user?**

- It reduces the privileges available to the application process.
- It is one control, not a complete security boundary by itself.

**Q4. Why keep downloaded models in a volume?**

- Container replacement should not require downloading the same model again.
- Removing the volume also removes those saved model files.

**Q5. Are mutable image tags enough for a reproducible deployment?**

- No; A tag can point to new content later.
- Resolve approved digests and use a tested rollout and rollback process for a real deployment.

## A few things to remember

- Liveness: `/actuator/health/liveness`.
- Readiness: `/actuator/health/readiness`.
- Readiness checks Ollama reachability; it does not prove a model is installed or generate an answer.
- The Compose ports are local.
- This small operational example is not authenticated; do not expose its chat or metrics publicly.
- Image tags are convenient teaching defaults and can move.
- Pin image digests and add authentication, TLS, backup and rollout policies before real deployment.

- [Tests](src/test/java/com/example/lesson23/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Stop Ollama.
- Liveness should stay UP while readiness becomes DOWN.
- Restart it and check recovery.

Next: [College knowledge assistant](../24-college-knowledge-assistant/README.md).

- Official reference: [Spring AI - Production operations](https://docs.spring.io/spring-ai/reference/observability/index.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
