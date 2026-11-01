# 05 - Streaming responses

> **Lesson 05** · **Local port: 8085** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- A college help desk reply may take a while.
- Let's show it as it arrives, rather than waiting for the whole answer.

- Think of watching someone type a reply.
- A stream sends small pieces of text.
- Here we use Server-Sent Events (SSE), an HTTP format for sending events from the server.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson05/LessonController.java).
- The key part is:

```java
client.prompt().user(request.question()).stream().content()
```

- `stream().content()` returns a `Flux<String>`: text pieces that arrive over time.
- We wrap each piece in a `token` event, then send `done`.
- If generation fails after HTTP headers are sent, we send an `error` event.
- A client disconnect cancels the subscription.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **05-streaming-responses**.
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
- Open **http://localhost:8085** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "Explain what a college help desk does."
}
```

From another terminal in the same folder:

```powershell
curl.exe -N -i http://localhost:8085/api/stream -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```text
event:token
data:Hello

event:done
data:complete
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson05/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8085/api/stream` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send `request.json` to `/api/stream`.
   - For a visible live demonstration, use the existing **curl -N** command in a terminal beside the source code.
2. Point out `event:token` as text arrives and `event:done` when the reply completes.
   - Postman can also send the same POST body; its display may buffer depending on the client.
3. Cancel a long request with Ctrl+C.
   - Show the cancellation test separately to verify that the provider publisher receives cancellation; do not claim the model stops computing instantly.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Why use Flux<String> for streaming?**

- A Flux represents multiple values arriving over time.
- The controller can forward pieces instead of waiting for a single complete string.

**Q2. Is an SSE token event necessarily one model token?**

- No; token is this application’s event name.
- Each data value may contain part of a word, several tokens or a larger text chunk.

**Q3. How do SSE and WebSocket differ?**

- SSE carries server-to-client events over HTTP.
- WebSocket supports a bidirectional connection; this lesson only needs the server to stream its reply.

**Q4. Why send an error event instead of changing the HTTP status?**

- Once response headers are sent, the status may already be committed.
- The streaming protocol needs an in-band error signal.

**Q5. Does disconnecting guarantee immediate inference cancellation?**

- It cancels the application subscription and propagates upstream.
- The provider and model runtime determine how quickly their computation actually stops.

## A few things to remember

- Use `curl -N` to display events immediately.
- Pieces are not guaranteed to be whole words.
- There is a 45-second idle timeout.
- Cancellation stops the application subscription; how quickly inference stops depends on Ollama.
- The tests check chunks, error events and upstream cancellation without a real model.

- [Tests](src/test/java/com/example/lesson05/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Change the question.
- Stop curl with Ctrl+C while a long answer is arriving.
- What happens to the subscription?

Next: [Chat memory](../06-chat-memory/README.md).

- Official reference: [Spring AI - Streaming responses](https://docs.spring.io/spring-ai/reference/api/chatclient.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
