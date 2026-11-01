# 25 - Provider capabilities (optional)

## What problem are we solving?

Switching models is easy to write in code, but the new model may support different features or produce different results.

Two library assistants can answer the same question differently. Compare them on the same task, and check which equipment each actually has: text, images, tools or native JSON constraints.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson25/LessonController.java). The key part is:

```java
client.prompt().user(q.question()).call().entity(Recommendation.class,spec -> spec.useProviderStructuredOutput())
```

`compare` sends the same question to the default and configured comparison models. `nativeJson` passes a schema through the provider's structured-output option, unlike the basic mapping in lesson 04. `/api/capabilities` makes unsupported features explicit.

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **25-provider-extensions**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8105** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "Recommend one beginner-friendly library book with a short reason."
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8105/api/native-json -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "title": "A Book",
  "reason": "Easy to read"
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson25/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8105/api/native-json` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Open **GET** `/api/capabilities` and show which features this integration implements. Send `request.json` to `/api/native-json` and inspect the returned `title` and `reason`.
2. Send the same body to **POST** `/api/compare`. The default configuration calls the same model twice; say so explicitly instead of presenting it as a two-model comparison.
3. For a real comparison, install a second compatible local chat model before class and set `OLLAMA_COMPARISON_MODEL` in the run environment. Use the same questions and compare correctness and latency, not only writing style.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Does changing a model name guarantee feature compatibility?**

No. Tool use, image input, output schemas and settings depend on the selected provider and model.

**Q2. How does native structured output differ from basic entity mapping?**

It sends the output schema as a provider-level constraint. Basic mapping asks for a shape and parses text without that extra enforcement.

**Q3. Does schema enforcement guarantee factual accuracy?**

No. A perfectly shaped response can contain a wrong title or reason. Validate fields and evaluate the substance separately.

**Q4. What should be held constant in a model comparison?**

Use the same task, input data, grading rules and comparable options. Record the exact models so differences can be interpreted.

**Q5. Does this project generate audio or images?**

No. It compares chat models and exercises native JSON output. Other modalities need an appropriate model and a separately implemented integration.

## A few things to remember

- Use `/api/compare` with the same request. Set `OLLAMA_COMPARISON_MODEL` to another installed local model; by default both calls use the same model so no extra download is hidden.
- Image input needs a vision model (lesson 10). Tool support also depends on the model. Native JSON constraints can help shape, but do not verify facts.
- This Ollama integration does not provide audio or image generation. Those need a suitable separate provider; no paid provider, API key or cloud call is enabled here.

[Tests](src/test/java/com/example/lesson25/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Compare two installed models on the same five questions. Record correctness, latency and supported features separately.

You have reached the end of the course. Revisit the capstone and test it with your own approved documents.

Official reference: [Spring AI - Provider capabilities (optional)](https://docs.spring.io/spring-ai/reference/api/structured-output.html). Use the [2.0.2 baseline](../VERSIONS.md).
