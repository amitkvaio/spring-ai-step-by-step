# 08 - Tool calling

> **Lesson 08** · **Local port: 8088** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- A customer asks for the price of a pen.
- The model should check the shop's price list instead of guessing.

- A shop assistant looks up a price before answering.
- Our Java `ProductTools.price` method is that price list.
- A tool is an application function made available to the model.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson08/LessonController.java).
- The key part is:

```java
@Tool(description="Look up the current demo shop price in INR for PEN or NOTEBOOK. Never guess prices.")
public BigDecimal price(String sku)
```

- `.tools(new ProductTools())` advertises the method.
- The model requests `price` with a SKU.
- Spring AI executes the Java method, sends the result back, and asks the model for its final reply.
- The model does not execute arbitrary Java or access a database directly.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **08-tool-calling**.
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
- Open **http://localhost:8088** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "What is the current price of PEN?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8088/api/chat -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "A pen costs INR 10."
}
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson08/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8088/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Ask for the price of PEN using `request.json`.
   - Set a breakpoint in `ProductTools.price` so students can see Java executing the lookup.
2. Ask for NOTEBOOK and compare the answer with its fixed INR 40 price.
   - If the model skips the tool, show that the breakpoint did not trigger rather than calling the answer a verified lookup.
3. Run the real tool-round-trip and endless-tool-request tests.
   - Show that the guard allows at most four **model calls**, rather than confusing this with a count of tool invocations.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Who executes the Java tool method?**

- The application executes it through Spring AI.
- The model proposes a tool name and arguments; it does not execute arbitrary Java.

**Q2. What information does the model receive about a tool?**

- A definition describing its name, purpose and argument shape.
- Good descriptions help the model decide when to request it.

**Q3. Does registering a tool guarantee it is called?**

- No; the model decides whether to request an available tool.
- Inspect the actual execution when correctness depends on a lookup.

**Q4. Why bound a tool loop?**

- Repeated tool requests can waste time and resources or fail to terminate.
- This example bounds model iterations; production code may also need per-tool limits.

**Q5. How should tools validate arguments?**

- Check them in application code before any operation.
- Schema guidance does not replace product validation, authorization or safe error handling.

## A few things to remember

- The demo has PEN at INR 10 and NOTEBOOK at INR 40.
- Unknown products are rejected.
- Use a model that supports tools.
- A small model may answer without choosing the tool.
- A per-turn advisor allows at most four model calls.
- Tests cover the real tool callback and an endless sequence of tool requests.

- [Tests](src/test/java/com/example/lesson08/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Add PENCIL to the price list and ask for its price.
- Check that the reply uses Java's value.

Next: [Service tools and authorization](../09-secure-service-tools/README.md).

- Official reference: [Spring AI - Tool calling](https://docs.spring.io/spring-ai/reference/api/tools.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
