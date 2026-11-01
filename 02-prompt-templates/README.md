# 02 - Prompt templates

> **Lesson 02** · **Local port: 8082** · **Run with your IDE or Maven**

- In the first project, we sent a question directly.
- Now let's reuse the same instruction with different values.

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

Imagine a librarian uses this card:

> Explain ___ to a reader named ___. Use a library example.

- The instruction stays the same.
- Only the topic and reader change.
- A **prompt template** does exactly that: it fills named blanks before sending the message to the model.

## The important code

Open [LibraryController.java](src/main/java/com/example/lesson02/LibraryController.java), method `explain`:

```java
String answer = chatClient.prompt()
        .user(user -> user.text("Explain {topic} to a reader named {name}. Use a library example.")
                .param("topic", request.topic()).param("name", request.name()))
        .call()
        .content();
```

- `text(...)` is our reusable instruction card.
- `{topic}` and `{name}` are the blanks.
- `param(...)` fills each blank using the matching name.
- `call().content()` gets the completed reply.

- The constructor also sets `defaultSystem(...)`: act as a librarian, use simple English, and don't invent library policies.
- A **system message** gives application instructions.
- A **user message** carries this request.

With `name=Asha` and `topic=catalogs`, the user message becomes:

> Explain catalogs to a reader named Asha. Use a library example.

```text
POST /api/explanations -> LibraryController.explain -> fill blanks -> Ollama -> answer
```

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **02-prompt-templates**.
- You don't need project 01 running.

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
- The address is **http://localhost:8082**.
- Stop with Ctrl+C before rebuilding.

## Try it

[request.json](request.json) contains:

```json
{"name":"Asha","topic":"catalogs"}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8082/api/explanations -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, use `curl` instead of `curl.exe`.

Example reply; the wording can change:

```json
{"answer":"Asha, a catalog is a list that helps you find books in a library."}
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson02/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8082/api/explanations` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send the existing request with `name=Asha` and `topic=catalogs`.
   - Show the `answer` field and connect it to the librarian example.
2. Change only the name to Ravi and then only the topic to book reservations.
   - Explain that Java fills the same named blanks for each request.
3. Open `LibraryController.explain` and the test that checks the rendered messages.
   - Students should distinguish the fixed system instruction from the current user message.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Why use a prompt template instead of repeated string concatenation?**

- Named blanks keep reusable wording and changing inputs easy to inspect.
- This reduces formatting mistakes and makes the rendered message testable.

**Q2. What is the difference between system and user messages?**

- The system message gives application instructions.
- The user message carries the current request; neither role replaces Java authorization checks.

**Q3. Does filling template variables make user input safe?**

- No; A value can still contain instructions attempting to change model behavior.
- Validate inputs and enforce sensitive operations outside the prompt.

**Q4. Does a template create conversation memory?**

- No; it formats the values supplied for one request.
- Earlier requests are not included automatically.

**Q5. What should a template test assert?**

- Assert the rendered text and correct message roles before generation.
- Do not require a live model to produce an exact sentence.

## A few things to remember

- The name must be nonblank and at most 80 characters; the topic must be nonblank and at most 200.
- Invalid input returns **400**.
- A template formats text.
- It doesn't stop a user from putting unwanted instructions in a value.
- A system message guides the model; it doesn't verify library policies or enforce access rights.
- The [HTTP tests](src/test/java/com/example/lesson02/LessonHttpTest.java) check the exact rendered message and its roles using a local fixture.
- **HTTP 502:** Check that Ollama is running and the model is installed.

## Try a small change

- Change the name to Ravi and the topic to book reservations.
- You shouldn't need to change the Java template.

**Want one more step?**

- Add a `readingLevel` field, a `{readingLevel}` blank and a matching `param(...)`.
- Update the request and test together.

**Does a template remember Asha?**

No; it only uses values from the current request.

- Next: [03 - Model options](../03-model-options/README.md).
- We'll control variation and the output budget.

- Official references: [ChatClient templates](https://docs.spring.io/spring-ai/reference/api/chatclient.html), [Ollama](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
