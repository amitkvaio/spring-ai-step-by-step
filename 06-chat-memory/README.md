# 06 - Chat memory

> **Lesson 06** · **Local port: 8086** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- Tell an assistant your name, then ask for it in another request.
- Without earlier messages, the model has nothing to look back at.

- A help desk clerk keeps a few notes between visits.
- Chat memory works by sending selected earlier messages with the next question.
- It does not retrain the model.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson06/LessonController.java).
- The key part is:

```java
client.prompt().user(question).advisors(a -> a.param(ChatMemory.CONVERSATION_ID,id)).call().content()
```

- `MessageChatMemoryAdvisor` adds stored messages before the model call and saves the new turn afterwards.
- We keep six messages per server-created HTTP session.
- Reuse the session cookie to continue the conversation.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **06-chat-memory**.
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
- Open **http://localhost:8086** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "My name is Asha."
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8086/api/chat -c cookies.txt -b cookies.txt -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "Hello, Asha!"
}
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson06/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8086/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. In Postman, send `{"question":"My name is Asha."}`, then `{"question":"What is my name?"}` to the same URL.
   - Keep the same localhost session cookie.
2. Show the `JSESSIONID` cookie and explain why the second prompt includes selected earlier messages.
   - Exact generated recall still depends on the model.
3. Send **DELETE** to `/api/chat` using the same cookie.
   - Ask the follow-up again and show the cleared memory in the debugger or deterministic test.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Does chat memory train the model?**

No; it stores selected messages in the application and sends them again with future calls.

**Q2. How is chat memory different from a full chat history?**

- Memory contains the context selected for future model requests.
- A full history is an audit or product record and can need separate durable storage.

**Q3. Is a six-message window a six-token limit?**

- No; messages have different sizes.
- A message-count bound helps limit history but is not an exact token budget.

**Q4. Why derive the conversation ID from the server session?**

- It prevents every visitor from sharing one fixed memory key.
- A session boundary still does not authenticate a person.

**Q5. What happens on application restart?**

- The in-memory messages disappear.
- A multi-instance application needs an appropriate shared repository and ownership rules.

## A few things to remember

- Run curl with `-c cookies.txt -b cookies.txt`, then ask "What is my name?"
- using the same cookie file.
- `DELETE /api/chat` clears your session's memory.
- Session expiry also clears it after 30 minutes.
- History is held in this process and disappears on restart.
- A session ID separates conversations; it is not user authentication.
- Turns are serialized in this small demo.

- [Tests](src/test/java/com/example/lesson06/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Ask several questions, then ask about the earliest one.
- Why might it have fallen outside the six-message window?

Next: [Advisors](../07-advisors/README.md).

- Official reference: [Spring AI - Chat memory](https://docs.spring.io/spring-ai/reference/api/chat-memory.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
