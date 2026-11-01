# 07 - Advisors

## What problem are we solving?

The help desk needs the same request check on every AI call. Copying that check into each method would be easy to forget.

Imagine a supervisor checks a form before the clerk handles it, then counts completed work afterwards. An advisor wraps a model call in those before-and-after steps.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson07/LessonController.java). The key part is:

```java
var response=chain.nextCall(request);
completed.incrementAndGet();
return response;
```

`DeskPolicyAdvisor` checks for a 12-digit account number before calling `chain.nextCall`. If the check fails, the model is never contacted. After a successful call, it increments the completed counter. Lower advisor order values run earlier on the request path.

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **07-advisors**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8087** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "How can the help desk help me?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8087/api/chat -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "The help desk can point you to the right department."
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson07/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8087/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send an ordinary help-desk question and show its reply. Put a breakpoint before and after `chain.nextCall` to show the advisor wrapping the call.
2. Send `{"question":"Account 123456789012"}`. Show HTTP **400** and explain that the model is not called for this rejected input.
3. Run `blocksBeforeModelCall` and `runsAfterSuccessfulModelCall` from `LessonTest` to show the controlled before/after assertions.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What is an advisor?**

It processes a ChatClient request or response around a model call. It can modify data, add reusable behavior, or stop the chain.

**Q2. How does advisor ordering work?**

Lower order values run earlier on the request path. The response returns through the wrapping chain in reverse order.

**Q3. What happens if an advisor does not call the next chain element?**

The remaining chain and model are not invoked. The advisor must return a suitable response or throw a handled error.

**Q4. Does this account-number rule provide complete data-loss prevention?**

No. It only detects one narrow 12-digit pattern. Sensitive-data policy needs broader controls, testing and access enforcement.

**Q5. Why test rejection before model invocation?**

It proves the boundary is enforced before any text reaches the provider. Testing only the final answer could hide an earlier data disclosure.

## A few things to remember

- A question containing a 12-digit number returns 400. This is a narrow classroom rule, not a complete sensitive-data detector.
- Advisors can inspect both sides of a call. An exception prevents later steps from running.
- The tests verify both blocking before the model and counting after success.

[Tests](src/test/java/com/example/lesson07/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Add another small form rule, such as a maximum word count. Test that rejected input never reaches the model.

Next: [Tool calling](../08-tool-calling/README.md).

Official reference: [Spring AI - Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html). Use the [2.0.2 baseline](../VERSIONS.md).
