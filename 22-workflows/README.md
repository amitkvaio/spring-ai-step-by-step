# 22 - Bounded workflows

## What problem are we solving?

A college help desk handles several kinds of requests. We need a clear next step, not an assistant running actions indefinitely.

A receptionist routes a question to a department. The model suggests a route, but Java decides which steps are allowed.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson22/LessonController.java). The key part is:

```java
case REVIEW -> new Result(Route.REVIEW,"A staff member must review this request. No action was performed.",true);
```

The first call maps the question to LIBRARY, SUPPORT or REVIEW. Library hours come from a fixed Java branch. Support drafts one reply in a second call. Requests for changes go to REVIEW and perform no action. Every path has at most two model calls.

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **22-workflows**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8102** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "Please change my account details."
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8102/api/workflow -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "route": "REVIEW",
  "answer": "A staff member must review this request. No action was performed.",
  "requiresHumanApproval": true
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson22/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8102/api/workflow` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Submit a library-hours question, a general support question and an account-change request in turn. Show LIBRARY, SUPPORT and REVIEW routes if the model classifies them as instructed.
2. For REVIEW, point to `requiresHumanApproval:true` and explain that no write has occurred. For SUPPORT, show the classifier call followed by the drafting call.
3. Run the invalid-route and two-step tests to show fixed boundaries independently of live classification quality. A wrong classification is still possible and has no automatic write capability.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. How does this workflow differ from an open-ended agent loop?**

Java defines a small set of branches and maximum steps. It does not let the model keep choosing arbitrary actions indefinitely.

**Q2. What is the maximum model-call count for a request?**

Two: one classification and, for SUPPORT, one draft. The other branches need only the classification call.

**Q3. Does a generated route grant permission to perform an action?**

No. It is a suggestion used by explicit application branches. No branch in this example can perform a write.

**Q4. What does requiresHumanApproval mean here?**

The application reports that staff review is needed. It does not implement a durable approval queue or claim that an action has been completed.

**Q5. How should an unknown route be handled?**

Fail closed with a safe error. Silently treating an unrecognized value as permission to run a default action is unsafe.

## A few things to remember

- A generated route is not authorization. Even a wrong route has no write operation available here.
- Invalid or missing routes fail closed with 502. The tests cover unknown routes and the two-call limit.
- This is an explicit workflow. It is not an autonomous agent framework or a production approval queue.

[Tests](src/test/java/com/example/lesson22/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Add a read-only FEES route backed by fixed approved information. Keep changes and payments out of all automatic branches.

Next: [Production operations](../23-production/README.md).

Official reference: [Spring AI - Bounded workflows](https://docs.spring.io/spring-ai/reference/api/chatclient.html). Use the [2.0.2 baseline](../VERSIONS.md).
