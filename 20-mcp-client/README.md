# 20 - MCP client

## What problem are we solving?

Our assistant needs the shop's published tools. We should discover them through MCP instead of importing the shop's source.

A clerk contacts the standard shop counter. The MCP client manages that connection, discovers tool definitions, and adapts them to Spring AI callbacks.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson20/LessonController.java). The key part is:

```java
.toolCallbacks(tools).call().content()
```

The MCP client starter connects to the configured server and supplies `ToolCallbackProvider`. `/api/tools` lists the discovered names. `/api/chat` gives those callbacks to the model. A tool request travels to the MCP server, and its result comes back for the final reply.

## Run it

Use the [one-time setup](../README.md#set-up-once). Open a terminal inside **20-mcp-client**. Each project has its own POM and wrapper.

Start a compatible shop MCP service first. The default is lesson 19 at `http://localhost:8099`; you can use another trusted server through `MCP_SERVER_URL`. This project has no sibling source dependency. It needs the external MCP service running, just as a database client needs its database.

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

Wait for `Started LessonApplication`. Open **http://localhost:8100** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "What is the current price of PEN?"
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8100/api/chat -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "A pen costs INR 10."
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson20/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8100/api/chat` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

The configured MCP service must be running before starting this client. The default is lesson 19 on port 8099.

Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Start lesson 19 first at port 8099, then start this client at port 8100. Keep both consoles visible to explain the two independently running processes.
2. Open **GET** `http://localhost:8100/api/tools` and show the discovered tool name before sending a model question.
3. Send the price question from `request.json`. Inspect the breakpoint in the server’s price method to show the remote lookup. If the model does not request a tool, do not present a generated price as proof of MCP execution.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What is the role of an MCP client?**

It connects to an MCP server, discovers capabilities and invokes them through the protocol. It does not import that server’s Java implementation.

**Q2. How do discovered tools reach ChatClient?**

The integration adapts remote tool definitions into ToolCallback instances. The application supplies them through a ToolCallbackProvider.

**Q3. Is an MCP session a per-user security boundary?**

Not automatically. These starter-managed clients are application-scoped, so user authorization must be designed separately.

**Q4. Who manages client connections on shutdown?**

The starter manages the configured clients and closes their resources with the application context.

**Q5. Should an assistant trust every remote tool description?**

No. Descriptions and returned text are untrusted inputs. Connect to approved servers and enforce what operations callers may perform.

## A few things to remember

- Open `http://localhost:8100/api/tools` to inspect discovery before asking a question. The request timeout is five seconds.
- The starter owns and closes its clients. Its MCP session is application-scoped, not a user authorization boundary.
- Connect only to a trusted read-only server for this lab. A remote tool description is untrusted text, not permission to perform arbitrary actions.

[Tests](src/test/java/com/example/lesson20/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Stop the MCP server and observe connection failure. Restart it and the client, then inspect the tool list again.

Next: [Security and data boundaries](../21-security/README.md).

Official reference: [Spring AI - MCP client](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html). Use the [2.0.2 baseline](../VERSIONS.md).
