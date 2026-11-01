# 19 - MCP server

## What problem are we solving?

Different assistants should be able to use the same shop price lookup without copying its Java code.

The shop opens a standard service counter. Model Context Protocol (MCP) defines how clients discover and call tools at that counter. A server can expose tools without running an LLM.

## The important code

Open [LessonController.java](src/main/java/com/example/lesson19/LessonController.java). The key part is:

```java
MethodToolCallbackProvider.builder().toolObjects(new ShopTools()).build()
```

`shopTools` publishes the annotated `price` method. The MCP WebMVC starter provides the `/mcp` endpoint using Streamable HTTP. This example advertises only a read-only price tool.

## Run it

You need JDK 21 or a compatible newer JDK. This server needs no model or API key. Open a terminal inside **19-mcp-server**. Each project has its own POM and wrapper.



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

Wait for `Started LessonApplication`. Open **http://localhost:8099** for a small status response. Stop with Ctrl+C before rebuilding. Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8099/api/catalog
```

On macOS/Linux, replace `curl.exe` with `curl`. Example response:

```json
{
  "PEN": 10,
  "NOTEBOOK": 40,
  "currency": "INR"
}
```

## Classroom demo

Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson19/LessonApplication.java). Or use the existing **Maven + curl** commands above. Follow the lesson's prerequisite setup first. Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `GET` |
| URL | `http://localhost:8099/api/catalog` |
| Body | None |
| Authorization | None for this local lesson |


Follow the sequence below before sending the main request. Click **Send** and display the response body and status beside the source code. Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Run this server without Ollama. Open **GET** `http://localhost:8099/api/catalog` in the browser and show the demo price data.
2. Explain that `/api/catalog` is an ordinary REST preview. The actual MCP endpoint is `/mcp`; showing the catalog alone does not demonstrate protocol discovery.
3. Keep this server running and start lesson 20 for the protocol demonstration. Show the discovered tool name there and place a breakpoint in this server’s `ShopTools.price` when the client invokes it.

Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting. Ports and sample response formats are documented above. Generated wording and similarity scores can vary.

For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests. Clearly call this a **deterministic test demonstration**, not a live model response. See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What does an MCP server expose?**

Standardized tools, resources or prompts through a protocol. This server exposes one controlled read-only price tool.

**Q2. Does an MCP server need an LLM?**

No. It can publish application capabilities without generating text. A separate client or assistant decides how to use them.

**Q3. Is the catalog REST endpoint the MCP endpoint?**

No. The catalog is a teaching preview. MCP clients communicate through the configured /mcp protocol endpoint.

**Q4. Which transport is used here?**

Streamable HTTP through the WebMVC starter. The project does not use the deprecated HTTP/SSE transport.

**Q5. Does the starter automatically authorize remote users?**

No. This example binds to localhost and exposes only a demo lookup. Authentication and authorization are required before wider exposure.

## A few things to remember

- No Ollama model or API key is needed for the server. `/api/catalog` is a plain REST preview; it is not the MCP protocol.
- The MCP endpoint is `http://localhost:8099/mcp`. Lesson 20 discovers and calls its tool.
- The server is bound to localhost and has no authentication. Put a real access boundary in front before exposing it elsewhere. Deprecated HTTP/SSE transport is not used.

[Tests](src/test/java/com/example/lesson19/LessonTest.java) run without paid APIs or a real model. See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

Add PENCIL to the tool and check that a connected MCP client can read its price.

Next: [MCP client](../20-mcp-client/README.md).

Official reference: [Spring AI - MCP server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html). Use the [2.0.2 baseline](../VERSIONS.md).
