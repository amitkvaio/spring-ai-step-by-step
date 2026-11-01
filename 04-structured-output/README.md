# 04 - Structured output

> **Lesson 04** · **Local port: 8084** · **Run with your IDE or Maven**

- So far, our model returns a paragraph.
- Let's get fields that our Java code can work with.

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- Imagine a support clerk reads, "I cannot access my account."
- The clerk fills in a form with a short summary and a priority.

- We want the model to fill in that same form.
- This is **structured output**: an answer with a known shape.

## The important code

- Open [TicketController.java](src/main/java/com/example/lesson04/TicketController.java), method `classify`.
- Our form is a Java record:

```java
public record Ticket(@NotBlank @Size(max = 160) String summary, @NotNull Priority priority) {}
```

- `Priority` allows `LOW`, `NORMAL` or `HIGH`.
- Instead of asking for plain text, we use:

```java
ticket = chatClient.prompt().user(request.issue()).call().entity(Ticket.class);
```

- `.entity(Ticket.class)` asks for ticket-shaped output and parses it into a `Ticket`.
- The system message asks for HIGH when an account is inaccessible, LOW for cosmetic issues, and NORMAL otherwise.
- The method then calls `validator.validate(ticket)` to check required fields.
- Parsing JSON alone doesn't check those rules.
- Bad JSON or missing fields return **502**.
- A valid ticket becomes our JSON response.

```text
POST /api/tickets -> TicketController.classify -> Ollama -> parse Ticket -> validate -> JSON
```

- The basic `.entity(...)` call asks the model to follow the shape; it doesn't force it.
- Even a valid ticket can contain a wrong classification.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **04-structured-output**.

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

- Wait for `Started LessonApplication`.
- The address is **http://localhost:8084**.
- Stop with Ctrl+C before rebuilding.

## Try it

[request.json](request.json) contains:

```json
{"issue":"I cannot access my account."}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8084/api/tickets -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, use `curl` instead of `curl.exe`.

Expected shape:

```json
{"summary":"Cannot access account","priority":"HIGH"}
```

- The summary wording can change.
- The instruction requests HIGH here, but check the actual classification too.

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson04/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8084/api/tickets` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send the account-access issue in `request.json`.
   - Show the `summary` and `priority` fields, then point to the `Ticket` record.
2. Change the issue to a cosmetic problem.
   - Compare the generated priority with the LOW rule in the system instruction; review the actual classification.
3. Run `LessonHttpTest` in the IDE to show malformed and incomplete generated-output cases returning **502**.
   - Explain that these tests use a fixture, not a live model failure chosen on demand.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. How does entity(Ticket.class) differ from content()?**

- content() returns generated text.
- entity(...) adds output-shape guidance and parses the complete reply into the requested Java type.

**Q2. Does parsing JSON validate business rules?**

- No; parsing constructs an object.
- This project separately uses Validator to check summary length, nonblank text and required priority.

**Q3. Is the basic entity call a strict provider-side schema guarantee?**

- No; the basic mapping path is best effort.
- Provider-native structured output is a separate capability, explored in lesson 25.

**Q4. Why use an enum for priority?**

- It gives Java code a small set of allowed values.
- Invalid or missing generated values still require parsing and validation failure handling.

**Q5. Can a valid HIGH priority authorize an account change?**

- No; correct shape does not prove correct classification or permission.
- Application rules and authenticated authorization must control any action.

## A few things to remember

- A blank issue or one over 1,000 characters returns **400**, before calling the model.
- A summary must be nonblank and at most 160 characters.
- Priority must be present.
- **HTTP 502:** Check Ollama first.
- If it's running, the model may have returned unusable ticket fields.
- The [HTTP tests](src/test/java/com/example/lesson04/LessonHttpTest.java) check valid mapping, malformed JSON, missing fields and provider failures using a local fixture.
- This app only returns a suggested ticket.
- It doesn't save it or change an account.

## Try a small change

- Report: "The profile icon has the wrong color."
- Our instruction asks for LOW for that cosmetic issue.
- Check whether the model follows it.

**Want one more step?**

- Add a required `department` field.
- Update the record, instruction and test fixture.
- Also test a reply with the department missing.

**Does valid JSON mean the answer is correct?**

- No; parsing checks the shape.
- Validation checks field rules.
- Neither proves the model's judgment.

- Next: [05 - Streaming responses](../05-streaming-responses/README.md).
- We'll show text while it arrives instead of waiting for the full reply.

- Official references: [Structured output](https://docs.spring.io/spring-ai/reference/api/structured-output.html), [Ollama](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
