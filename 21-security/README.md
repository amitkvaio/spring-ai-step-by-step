# 21 - Security and data boundaries

## What problem are we solving?

Alice and Bob use the same college assistant. Asking "show every record" must not give Alice access to Bob's private appointment.

A clerk checks identity before opening a file. Our application filters approved documents using the authenticated user before any text reaches the model.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson21/LessonController.java). The key part is:

```java
documents.stream().filter(d -> d.owner().equals("public") || d.owner().equals(owner)).toList()
```

`visible` selects public and owned documents. `ask` builds its context from that list, not a user-provided owner ID. Spring Security protects the API, and only staff can add documents. A simple email replacement demonstrates data minimization, not complete anonymization.

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **21-security**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8101** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "When is my scholarship appointment?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8101/api/ask -u alice:local-alice -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "Your scholarship appointment is Tuesday."
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson21/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8101/api/ask` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | **Basic Auth**: `alice` / `local-alice` |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Use Basic Auth `alice` / `local-alice` for **GET** `/api/documents`, then switch to `bob` / `local-bob`. Show Tuesday for Alice and Friday for Bob, alongside the public document.
2. Ask for all appointments as Alice. Open the context-building code or tests to show Bob’s private passage is excluded before the model call.
3. Send **POST** `/api/admin/documents` with `{"owner":"public","text":"The library closes at 17:00."}` as Alice and show **403**. Remove authentication from a protected request and show **401**. Use only the included classroom data.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. Why enforce ownership before building the prompt?**

Once another user’s text reaches the provider, a later output filter cannot undo that disclosure. Retrieve or select only authorized material first.

**Q2. Can a system message replace access control?**

No. Instructions can guide generation, but authenticated Java rules must decide which data and tools are available.

**Q3. Is a user-supplied owner field a trusted identity?**

No. Identity comes from the authenticated principal. This app ignores such claims when selecting a reader’s documents.

**Q4. Does removing email-like text make a prompt anonymous?**

No. The simple replacement illustrates one data-minimization step. Other identifying or sensitive information can remain.

**Q5. What must change before public deployment?**

Replace the public demo accounts, add a suitable identity system and TLS, and review access, retention, logging and operational controls.

## A few things to remember

- Use the local demo accounts `alice/local-alice`, `bob/local-bob`, and `staff/local-staff`. Replace them with a real identity provider before deployment.
- `GET /api/documents` shows only your permitted context. `/api/admin/documents` is staff-only.
- Prompt instructions are not access controls. This lab shows authorization before generation; it cannot guarantee that a model obeys every instruction.

[Tests](src/test/java/com/example/lesson21/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Ask for all appointments as Alice, then inspect the sent context or tests. Bob's Friday record must be absent.

Next: [Bounded workflows](../22-workflows/README.md).

Official reference: [Spring AI - Security and data boundaries](https://docs.spring.io/spring-ai/reference/api/tools.html). Use the [2.0.2 baseline](../VERSIONS.md).
