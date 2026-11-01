# Spring AI learning roadmap

Read in number order. Required lessons form the main path. Optional lessons need extra capabilities or infrastructure. All 25 practical folders are implemented. Infrastructure and live-model validation limits are recorded separately. See [PROGRESS.md](PROGRESS.md) for executed checks. Each practical lesson is an independent Maven project; no lesson imports another lesson's code.

## Foundation (required reading, no artificial application)

**F0 — AI vocabulary and architecture.** Objectives: distinguish AI (software performing tasks associated with intelligence), generative AI (creating content), and an LLM (a large language model trained to predict and generate text). Understand tokens (model-specific text pieces), a context window (the input and output budget for a call), and embeddings (numeric representations used to compare meaning). Prerequisites: Java and basic Spring Boot. Problem: unfamiliar terms hide what an AI call actually does. Example: a college help desk drafts an answer using a limited set of notes; it can still make mistakes. Application connection: `ChatClient` builds a request, `ChatModel` adapts it to a provider, and the provider runs inference. Read [the course introduction](README.md#before-lesson-01). Embeddings become runnable in lesson 11. A language model is not a database or a reliable calculator.

## Beginner

| No. / topic | Objectives | Prerequisites | Problem solved / simple example | Application / Maven folder | Path |
|---|---|---|---|---|---|
| 01 First AI chat | Configure provider; distinguish client, model, prompt and response | F0; JDK; Ollama | Draft a college help desk reply without hand-writing every answer | Help desk chat / `01-first-ai-chat` | Required |
| 02 Prompt templates and roles | Render named blanks; separate system and user messages | 01 | Reuse a librarian's instruction card for different visitors | Library explanation assistant / `02-prompt-templates` | Required |
| 03 Model options | Set temperature and output budget; understand provider-specific controls | 02 | A teacher requests a short answer in a chosen style | Classroom answer generator / `03-model-options` | Required |
| 04 Structured output | Map a reply into a Java record; validate fields; handle malformed output | 03 | A support clerk fills in a fixed ticket form | Support ticket classifier / `04-structured-output` | Required |
| 05 Streaming responses | Send incremental text; cancel a stream; handle errors after headers | 04; basic HTTP | A help desk displays a reply as it is written | SSE chat / `05-streaming-responses` | Required |
| 06 Chat memory | Keep selected messages; isolate conversation identifiers; enforce bounds | 05 | A clerk keeps selected notes between visits | Conversation assistant / `06-chat-memory` | Required |
| 07 Advisors | Process requests and responses; understand ordering and context | 06 | A desk supervisor checks a request before and after handling it | Request policy advisor / `07-advisors` | Required |
| 08 Tool calling | Expose a constrained application method; inspect arguments and results | 07 | A shop assistant checks the price list before answering | Product price assistant / `08-tool-calling` | Required |
| 09 Application tools and authorization | Validate arguments; scope access; distinguish reads and writes | 08; service layer | A shop clerk may read stock but needs permission to place an order | Inventory assistant with approved actions / `09-secure-service-tools` | Required |
| 10 Multimodal input | Combine image and text; explain media support and limits | 08; suitable model | A librarian reads a book-cover photo | Cover description assistant / `10-multimodal-input` | Optional; model-dependent |

## Intermediate

| No. / topic | Objectives | Prerequisites | Problem solved / simple example | Application / Maven folder | Path |
|---|---|---|---|---|---|
| 11 Embeddings and semantic search | Generate vectors; compare similarity; avoid mixing model dimensions | 04; basic vectors explained here | Find library questions with similar meaning despite different words | In-memory FAQ search / `11-embeddings-semantic-search` | Required; embedding model |
| 12 Persistent vector stores | Store vectors and metadata; search with filters; distinguish SQL and vector search | 11; Docker | A library catalog retains its index after restart | PGvector FAQ index, Compose / `12-vector-store` | Required; PostgreSQL extension, not a MySQL replacement |
| 13 Document ingestion | Load, transform, split and index; retain source metadata; make indexing repeatable | 12 | A librarian divides an approved handbook into findable passages | Handbook indexer / `13-document-ingestion` | Required |
| 14 Basic RAG | Retrieve passages before generation; cite sources; abstain when unsupported | 13 | A help desk looks up handbook pages before answering | Document question answering / `14-basic-rag` | Required |
| 15 Retrieval quality and advanced RAG | Tune chunks, top-k and thresholds; evaluate retrieval; add rewriting/reranking selectively | 14 | A librarian improves a poor catalog search before choosing pages | Retrieval comparison lab / `15-advanced-rag` | Required; rerankers optional/provider-dependent |
| 16 Evaluation and testing | Separate retrieval and generation quality; build labeled cases; keep model judges opt-in | 14 | A teacher grades answers against a rubric and verifies cited pages | Regression evaluation harness / `16-evaluation-testing` | Required |
| 17 Observability and budgets | Measure latency, usage and failures; explain missing provider usage; estimate cost | 08; 14 | A help desk tracks wait time and work performed | Metered assistant / `17-observability` | Required; billing metadata varies |
| 18 Resilience and rate limits | Set timeouts; bound retries; handle throttling; avoid retrying side effects | 17 | A clerk waits briefly and retries only safe requests | Resilient assistant / `18-resilience` | Required |

## Advanced

| No. / topic | Objectives | Prerequisites | Problem solved / simple example | Application / Maven folder | Path |
|---|---|---|---|---|---|
| 19 MCP server | Publish controlled tools through Model Context Protocol; choose transport | 09; HTTP/process basics | A shop publishes a standard service counter | Product MCP server / `19-mcp-server` | Required; transport/version-dependent |
| 20 MCP client | Discover tools; handle connection lifecycle; scope trust and credentials | 19 | A clerk contacts that standard counter | Assistant MCP client / `20-mcp-client` | Required; external server needed |
| 21 Security and data boundaries | Enforce identity in application code; defend retrieval/tool boundaries; redact sensitive data | 09; 14; 20 | A college clerk checks student access before opening records | Authenticated knowledge assistant / `21-security` | Required |
| 22 Bounded workflows | Route tasks; compose explicit steps; bound tool loops; require approval for writes | 18; 21 | A desk routes a request to the right department and checks completion | Routed college requests / `22-workflows` | Required; autonomous agent extensions optional |
| 23 Production operations | Package containers; configure readiness, secrets and limits; back up indexes; plan upgrades | 18; 21 | A college plans staffing, opening checks and recovery | Deployable assistant, Compose / `23-production` | Required |
| 24 College knowledge assistant capstone | Combine ingestion, vector search, sourced RAG, isolated memory, tools, typed output, security, resilience and metrics | Required lessons 01–23 | College help desk answers only from approved documents and checks opening hours with a tool | College help desk with Compose / `24-college-knowledge-assistant` | Required |
| 25 Provider extensions | Compare local models; try native JSON; identify unsupported audio/image generation | 10; 17; 23 | Choose the right desk equipment for a task | Capability lab / `25-provider-extensions` | Optional; provider-specific; no paid provider enabled |

## Scope and version boundaries

- Main provider: local Ollama. Download a chat model first. Use a separate embedding model from lesson 11 onward. Small local models may produce weaker structured replies and tool choices.
- A common interface does not guarantee equal model features. Image input, tools, native JSON schemas, audio, usage counts and options must be verified for the chosen model and provider.
- Use stable Spring AI 2.0.2 APIs. Preview 2.1 session-memory APIs and experimental agent integrations are outside this baseline. Recheck compatibility before adopting them.
- PostgreSQL/PGvector is selected for the vector-store lesson because it has a dedicated Spring AI integration. The course does not add a business database where none is needed.
- Tool flow: model proposes a tool call → application validates and executes Java → result returns to model → model drafts the answer. The model cannot directly execute arbitrary Java or access a database.
- The capstone must attach source IDs from retrieved metadata, enforce per-user document filters before retrieval, bound input/history/tools, and test unauthorized access and unsupported questions. It is not finished until those paths and deployment instructions are validated.

## Official topic references

Baseline: [Spring AI 2.0.2 reference](https://docs.spring.io/spring-ai/reference/index.html), [getting started and compatibility](https://docs.spring.io/spring-ai/reference/getting-started.html), [ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html), [Ollama](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html), [structured output](https://docs.spring.io/spring-ai/reference/api/structured-output.html). Consult the corresponding version's reference navigation for memory, advisors, tools, ETL, vector stores, RAG, MCP and observability when implementing those batches. Code is implemented for every lesson. Passing deterministic tests is separate from live model, database and container validation; consult PROGRESS.md.
