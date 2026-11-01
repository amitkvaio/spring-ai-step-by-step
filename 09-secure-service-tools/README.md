# 09 - Service tools and authorization

> **Lesson 09** · **Local port: 8089** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

A shop assistant may read stock, but placing an order should require an authorized person's explicit approval.

- Let the assistant check the shelf.
- Keep the order button in the application.
- The model receives only a read-only stock tool; a separate staff-only endpoint changes inventory.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson09/LessonController.java).
- The key part is:

```java
client.prompt().user(q.question()).advisors(a -> a.param("modelCallCount",new java.util.concurrent.atomic.AtomicInteger())).tools(new StockTools(inventory,user.getName())).call().content()
```

- `StockTools` calls `InventoryService` with the authenticated user's name.
- The name comes from Spring Security, not the prompt.
- Only staff can call `/api/admin/orders`, and that request must contain `confirm:true`.
- Repeating a request ID returns its original result without reducing stock twice.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **09-secure-service-tools**.
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
- Open **http://localhost:8089** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "How many pens are available?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8089/api/chat -u alice:local-alice -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "The demo shop has 10 pens available."
}
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson09/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8089/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | **Basic Auth**: `alice` / `local-alice` |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Select Postman **Basic Auth**, using `alice` / `local-alice`, and ask for PEN stock.
   - A fresh application starts with ten pens.
2. Create a second request: **POST** `/api/admin/orders`, raw JSON copied from `order.json`.
   - As Alice, show **403**.
   - Switch to `staff` / `local-staff` and send it to show an approved order.
3. Send the identical staff request again.
   - Show that the same request ID does not reduce stock twice.
   - Restarting this demo resets its in-memory inventory.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Why are only read-only tools exposed to the model?**

- Reading stock is a different permission from placing an order.
- The application keeps the write behind a separate staff-only endpoint and explicit confirmation.

**Q2. Where must user identity come from?**

- From authenticated server context, such as Principal.
- A user name in the prompt or a model-generated argument is not trustworthy identity.

**Q3. What is idempotency in this order example?**

Repeating the same authenticated request ID returns its original result instead of applying the stock change again.

**Q4. What is the difference between 401 and 403?**

- 401 means valid authentication is missing.
- 403 means the authenticated caller lacks the required permission.

**Q5. Does an in-memory idempotency map work across restarts?**

No; A real service needs durable idempotency records and transactional inventory changes, including a policy for conflicting payloads using the same key.

## A few things to remember

- Local classroom users: `alice/local-alice`, `bob/local-bob`, `staff/local-staff`.
- These are public demo credentials.
- Test approval with `order.json`, `/api/admin/orders`, and `-u staff:local-staff`.
- Alice gets 403; missing login gets 401.
- Orders and stock are in memory.
- A real service needs durable transactions and an identity provider.

- [Tests](src/test/java/com/example/lesson09/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Call the order endpoint twice with the same request ID.
- Stock should change once.
- Then try as Alice and confirm it is forbidden.

Next: [Multimodal input (optional)](../10-multimodal-input/README.md).

- Official reference: [Spring AI - Service tools and authorization](https://docs.spring.io/spring-ai/reference/api/tools.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
