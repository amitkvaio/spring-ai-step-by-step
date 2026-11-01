# Learn Spring AI, step by step

> **25 standalone lessons** · **Java 21** · **Spring AI 2.0.2** · **Default provider: local Ollama**

- You already know Java and Spring Boot.
- Let's use that knowledge to build small AI applications.

- Each project teaches one idea: start with a familiar problem, look at the code, and try a request.
- **All 25 projects are implemented.**
- **Start with 01 and follow the numbers.**

## Quick navigation

- **Learn:** [Lesson index](#start-here) · [AI basics](#before-lesson-01)
- **Run:** [One-time setup](#set-up-once) · [First project](#run-the-first-project) · [Common fixes](#settings-and-common-fixes)
- **Teach:** [Classroom setup](#classroom-setup) · [Interview FAQ](#interview-faq)

## Start here

| Order | Project | Port |
|---|---|---|
| 01 | [First AI chat](01-first-ai-chat/README.md) | 8081 |
| 02 | [Prompt templates](02-prompt-templates/README.md) | 8082 |
| 03 | [Model options](03-model-options/README.md) | 8083 |
| 04 | [Structured output](04-structured-output/README.md) | 8084 |
| 05 | [Streaming responses](05-streaming-responses/README.md) | 8085 |
| 06 | [Chat memory](06-chat-memory/README.md) | 8086 |
| 07 | [Advisors](07-advisors/README.md) | 8087 |
| 08 | [Tool calling](08-tool-calling/README.md) | 8088 |
| 09 | [Service tools and authorization](09-secure-service-tools/README.md) | 8089 |
| 10 | [Multimodal input (optional)](10-multimodal-input/README.md) | 8090 |
| 11 | [Embeddings and semantic search](11-embeddings-semantic-search/README.md) | 8091 |
| 12 | [Persistent vector stores](12-vector-store/README.md) | 8092 |
| 13 | [Document ingestion](13-document-ingestion/README.md) | 8093 |
| 14 | [Basic RAG](14-basic-rag/README.md) | 8094 |
| 15 | [Retrieval quality and advanced RAG](15-advanced-rag/README.md) | 8095 |
| 16 | [Evaluation and testing](16-evaluation-testing/README.md) | 8096 |
| 17 | [Observability, usage and cost](17-observability/README.md) | 8097 |
| 18 | [Timeouts, retries and rate limits](18-resilience/README.md) | 8098 |
| 19 | [MCP server](19-mcp-server/README.md) | 8099 |
| 20 | [MCP client](20-mcp-client/README.md) | 8100 |
| 21 | [Security and data boundaries](21-security/README.md) | 8101 |
| 22 | [Bounded workflows](22-workflows/README.md) | 8102 |
| 23 | [Production operations](23-production/README.md) | 8103 |
| 24 | [College knowledge assistant](24-college-knowledge-assistant/README.md) | 8104 |
| 25 | [Provider capabilities (optional)](25-provider-extensions/README.md) | 8105 |

- Every folder is a separate Maven application with its own wrapper.
- No project imports another lesson's code.
- Lesson 20 needs an external MCP server; lessons 12 and 24 need PostgreSQL unless you select their memory profile.

## Before lesson 01

- Suppose a student asks, "What does a college help desk do?"
- An AI model can draft an answer.
- But it cannot automatically check your college's current records.
- A confident answer can still be wrong.

Here are the few terms we need to begin:

| Term | Simple meaning |
|---|---|
| AI | Software that performs tasks associated with intelligence |
| Generative AI | AI that creates content, such as a reply |
| LLM | A large language model that generates text from learned patterns |
| Prompt | The input we send to the model, including instructions and questions |
| Token | A piece of text the model processes; it isn't always a whole word |
| Context window | The model's limit on input and generated output for a call |
| Embedding | Numbers that help compare the meaning of texts; we'll use these later |

- **Spring AI** connects our Java application to a model.
- **Ollama** runs the model locally.
- Sending a question uses the model; it doesn't train it.

```text
HTTP question -> Controller -> ChatClient -> Ollama model -> JSON reply
```

## Set up once

- We use **Java 21**, **Spring Boot 4.1.2**, **Spring AI 2.0.2** and **Maven 3.9.9** through the included wrappers.
- A compatible newer JDK also works.
- See [VERSIONS.md](VERSIONS.md) for verification details.

1. Install a compatible JDK.
   - Set `JAVA_HOME` to its folder.
2. Install [Ollama](https://ollama.com/download) and start it.
   - Use `ollama serve` if its service isn't already running.
3. Download the model: `ollama pull llama3.2:3b`.
4. Run `ollama list` to confirm it's installed.

- No cloud API key is needed.
- The model download uses disk space, and running it needs enough local memory and CPU/GPU power.
- The first Maven build also needs internet access.

## Run the first project

From the repository root, in **Windows PowerShell**:

```powershell
cd 01-first-ai-chat
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

On **macOS/Linux**:

```bash
cd 01-first-ai-chat
sh ./mvnw clean verify
sh ./mvnw spring-boot:run
```

- The address is **http://localhost:8081**.
- Use the POST example in the lesson README.
- Projects 05–25 also return a small status response at `/`.
- Stop the app with Ctrl+C before rebuilding it.

## Settings and common fixes

Each project's [application.properties](01-first-ai-chat/src/main/resources/application.properties) uses these environment variables:

| Variable | Default | Change it when... |
|---|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Your trusted Ollama service runs elsewhere |
| `OLLAMA_CHAT_MODEL` | `llama3.2:3b` | You want another installed chat model |
| `PORT` | The lesson's port above | That port is already in use |

- For example: `$env:PORT='8091'` in PowerShell, or `export PORT=8091` in Bash.
- Set it before starting the app.

- **400:** Check the JSON fields and allowed values.
- **502:** Check that Ollama is running and the model appears in `ollama list`.
- **405 in a browser:** Use POST with the supplied `request.json`, rather than GET.
- **Build/download error:** Check your JDK and access to Maven Central.

- The apps listen locally.
- Don't send private student data while experimenting.
- Never commit API keys.
- Lessons 12 and 24 include PostgreSQL/PGvector Compose setup.
- Lessons 23 and 24 also include container deployment examples.
- Their READMEs explain the extra steps.

## Tests and course notes

- `clean verify` builds each project and runs deterministic tests using model mocks, fixed vectors or local HTTP fixtures.
- No paid calls or running Ollama are needed for routine tests.
- The opt-in PostgreSQL test in lesson 12 is skipped unless `LIVE_VECTOR_TESTS=true`.
- These checks don't prove a real model's answer quality.

- [ROADMAP.md](ROADMAP.md): the full learning order and optional lessons.
- [TEACHING_GUIDE.md](TEACHING_GUIDE.md): classroom demonstrations and checkpoints.
- [PROGRESS.md](PROGRESS.md): checks actually run and work still pending.
- [Official Spring AI reference](https://docs.spring.io/spring-ai/reference/index.html): select version 2.0.2 when comparing APIs.

- When you're finished, `ollama stop llama3.2:3b` unloads the model.
- Optional: `ollama rm llama3.2:3b` removes its download.

## Classroom setup

- Every lesson now has a **Classroom demo** and an **Interview FAQ**.
- Keep the source code and Postman response visible beside each other.
- The existing Maven/curl approach remains available too.

1. **Prepare before class.**
   - Run that lesson's `clean verify`.
   - Install Java and download only the models it needs.
   - The [first lesson](01-first-ai-chat/README.md) includes the Windows D: drive installation and start/stop instructions.
2. **Start the dependencies.**
   - Most lessons need local Ollama.
   - Lesson 10 needs a vision model; lessons 11–15 and 24 need the embedding model for indexing/search.
   - Lesson 16's basic evaluation and lesson 19's server can run without inference.
   - Lesson 20 needs a trusted MCP server.
   - Projects 12 and 24 use PostgreSQL by default and offer a temporary memory profile.
3. **Choose how to run.**
   - In your IDE, import one lesson's POM and run its `LessonApplication.main`.
   - Alternatively, use its existing wrapper commands.
   - Both use the same port; stop one instance before starting another.
4. **Choose how to show the result.**
   - Use the lesson's Postman method, URL, JSON body and authentication.
   - For incremental streaming, use lesson 05's unbuffered curl command.
   - A browser address bar sends GET; it does not submit a POST JSON question.
5. **Explain one change at a time.**
   - Start with the sample request, show the response, change one input, and point to the code responsible.
   - For memory, keep the cookie or conversation key; for RAG, index before asking; for protected endpoints, show the correct login and a denied request.
6. **Keep a truthful fallback.**
   - Tests use mocks, fixed vectors or HTTP fixtures and do not need paid APIs.
   - If a model or database is unavailable, show those tests as a controlled demonstration.
   - They do not prove live generation, persistence or answer quality.
   - See [PROGRESS.md](PROGRESS.md) for validation already performed.

- For the capstone's full local/Compose setup and staff indexing, use [its deployment instructions](24-college-knowledge-assistant/DEPLOYMENT.md).
- Stop that lesson's app and dependencies when the demonstration is finished; avoid stopping unrelated applications or deleting volumes just to switch lessons.

## Interview FAQ

**Q1. What does Spring AI provide, and what does the model provide?**

- Spring AI provides application integration for prompts, providers, tools, memory and retrieval.
- The selected model performs inference; calling it does not train its weights.

**Q2. How do you choose between a prompt, memory, RAG and a tool?**

- A prompt describes the task.
- Memory supplies selected earlier turns, RAG supplies retrieved documents, and a tool obtains a controlled application result or action.
- Choose according to the missing information or capability.

**Q3. Does a common API make providers interchangeable?**

- No; verify the actual model's media, tools, schemas, options and usage reporting.
- Keep provider-specific behavior explicit and test it.

**Q4. How should an AI application be tested?**

- Test deterministic application contracts without live models first.
- Keep model and infrastructure checks opt-in, then separately evaluate retrieval quality, factual support, access boundaries and failure behavior.

**Q5. Can prompts enforce application security?**

- No; authenticate users and check permissions in Java before exposing data or tools.
- Treat user text, retrieved passages and generated output as untrusted input.
