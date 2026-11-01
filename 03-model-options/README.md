# 03 - Model options

We can already ask a question. Now let's control a couple of settings used to generate the answer.

## What problem are we solving?

Imagine a teacher says, "Explain why ice melts. Keep it short. A little variation in the wording is fine."

The prompt describes the task. **Model options** control parts of generation. Here we'll change two:

| Option | What it means |
|---|---|
| `temperature` | Influences variation in token choices. Lower often means less variation; it doesn't mean more accurate. |
| `maxTokens` | Our request's output budget. A token is a piece of text, so 96 tokens doesn't mean 96 words. |

## The important code

Open [ClassroomController.java](src/main/java/com/example/lesson03/ClassroomController.java), method `answer`:

```java
var options = OllamaChatOptions.builder()
        .temperature(request.temperature())
        .numPredict(request.maxTokens());
String answer = chatClient.prompt().user(request.question())
        .options(options).call().content();
```

- We create settings for this request, leaving the shared client unchanged.
- `numPredict(...)` is Ollama's setting for the output-token budget.
- Spring AI 2.0's `.options(...)` accepts the **builder**, so we don't call `.build()` here.
- `call().content()` returns the tutor's reply. The constructor's system message asks for one short, simple explanation.

For our sample, temperature `0.2` and budget `96` travel with the question:

```text
POST /api/answers -> ClassroomController.answer -> prompt + options -> Ollama -> answer
```

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **03-model-options**.

**Windows PowerShell**:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

**macOS/Linux**:

```bash
sh ./mvnw clean verify
sh ./mvnw spring-boot:run
```

Wait for `Started LessonApplication`. The address is **http://localhost:8083**. Stop with Ctrl+C before rebuilding.

## Try it

[request.json](request.json) contains:

```json
{"question":"Why does ice melt?","temperature":0.2,"maxTokens":96}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8083/api/answers -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, use `curl` instead of `curl.exe`.

Example reply; the wording can change:

```json
{"answer":"Ice melts when it absorbs enough heat to change from a solid into liquid water."}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson03/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8083/api/answers` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send `request.json`, then compare `maxTokens` 32 and 128 while keeping the question and temperature fixed. Show whether the answer finishes.
2. Compare temperature 0.0 and 0.8 over several requests. Tell students that variation is possible, not guaranteed, and correctness must be checked separately.
3. Send temperature 2.0. This application should return **400**, because its teaching contract accepts only 0.0–1.0.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Does temperature control factual accuracy?**

No. It influences sampling of the next tokens. A lower temperature can still produce a consistently wrong answer.

**Q2. Does maxTokens mean a word limit?**

No. This request field maps to Ollama numPredict, an output-token budget. It is not an input-character limit or the full context-window size.

**Q3. Why pass an options builder to ChatClient?**

The pinned Spring AI 2.0.2 request API accepts a ChatOptions builder. Passing a built options object follows a different API shape and does not compile here.

**Q4. Why create per-request options instead of changing shared state?**

Each caller can use its own settings without altering another request. The configured model remains the default unless overridden.

**Q5. Can the same option names be used with every provider?**

No. Portable abstractions exist, but numPredict is an Ollama-specific setting. Verify meanings and supported ranges for the selected provider and model.

## A few things to remember

- This app accepts temperature **0.0-1.0** and `maxTokens` **16-512**. Missing or invalid settings return **400**.
- Questions must be nonblank and at most 1,000 characters.
- A small budget may cut off an answer. It doesn't limit the input or set the whole context window.
- These are Ollama options. Other providers can use different settings.
- The [HTTP tests](src/test/java/com/example/lesson03/LessonHttpTest.java) check the options sent to the provider, using a local fixture. They don't expect particular AI wording.
- **502?** Check Ollama and the installed model name.

## Try a small change

Keep the question fixed. Compare budgets of 32 and 128 tokens. Then keep the budget fixed and compare temperatures 0.0 and 0.8 over a few calls.

**Will temperature 0 always give the same answer?** No. Less variation isn't a promise of identical output or correct facts.

Next: [04 - Structured output](../04-structured-output/README.md). We'll ask for named fields that Java code can use.

Official references: [Ollama options](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html), [ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html). Use the [2.0.2 baseline](../VERSIONS.md).
