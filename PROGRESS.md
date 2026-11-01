# Course progress

## Completed

All **25 independent Maven projects** are implemented. The user approved the short README style and authorized the remaining projects on **2026-11-02**. Each project has its own POM, Maven Wrapper, source, configuration, tests and conversational README. No project imports another lesson's source code.

The course includes the complete roadmap, verified version baseline, teaching guide, example requests, PostgreSQL/PGvector Compose setup, MCP server/client, operational container examples and the college knowledge-assistant capstone. Optional vision and provider-capability lessons are clearly labeled. No cloud provider or paid API call is enabled.

## Executed validation

**25 successful Maven builds. 80 tests passed. One opt-in real PostgreSQL/model integration test was skipped.** Tests use fixed model replies/vectors, mocks or local HTTP fixtures. Validation ran on Windows with JDK 24.0.2, Java release target 21, Maven Wrapper 3.9.9, Boot 4.1.2 and Spring AI 2.0.2.

| Project | Maven build | Tests passed | Packaged HTTP checks |
|---|---|---|---|
| [01-first-ai-chat](01-first-ai-chat/README.md) | `clean verify` passed | 4 | Passed; invalid input and missing Ollama |
| [02-prompt-templates](02-prompt-templates/README.md) | `clean verify` passed | 4 | Passed; invalid input and missing Ollama |
| [03-model-options](03-model-options/README.md) | `clean verify` passed | 4 | Passed; invalid input and missing Ollama |
| [04-structured-output](04-structured-output/README.md) | `clean verify` passed | 6 | Passed; invalid input and missing Ollama |
| [05-streaming-responses](05-streaming-responses/README.md) | `clean verify` passed | 3 | Passed; model fixture |
| [06-chat-memory](06-chat-memory/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [07-advisors](07-advisors/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [08-tool-calling](08-tool-calling/README.md) | `clean verify` passed | 3 | Passed; model fixture |
| [09-secure-service-tools](09-secure-service-tools/README.md) | `clean verify` passed | 6 | Passed; model fixture |
| [10-multimodal-input](10-multimodal-input/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [11-embeddings-semantic-search](11-embeddings-semantic-search/README.md) | `clean verify` passed | 3 | Passed; model fixture |
| [12-vector-store](12-vector-store/README.md) | `clean verify` passed | 2 (+1 opt-in skipped) | Passed; model fixture; memory profile |
| [13-document-ingestion](13-document-ingestion/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [14-basic-rag](14-basic-rag/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [15-advanced-rag](15-advanced-rag/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [16-evaluation-testing](16-evaluation-testing/README.md) | `clean verify` passed | 3 | Passed; model fixture |
| [17-observability](17-observability/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [18-resilience](18-resilience/README.md) | `clean verify` passed | 3 | Passed; model fixture |
| [19-mcp-server](19-mcp-server/README.md) | `clean verify` passed | 2 | Passed; model fixture; real MCP transport |
| [20-mcp-client](20-mcp-client/README.md) | `clean verify` passed | 2 | Passed; model fixture; real MCP transport |
| [21-security](21-security/README.md) | `clean verify` passed | 5 | Passed; model fixture |
| [22-workflows](22-workflows/README.md) | `clean verify` passed | 3 | Passed; model fixture |
| [23-production](23-production/README.md) | `clean verify` passed | 2 | Passed; model fixture |
| [24-college-knowledge-assistant](24-college-knowledge-assistant/README.md) | `clean verify` passed | 9 | Passed; model fixture; memory profile |
| [25-provider-extensions](25-provider-extensions/README.md) | `clean verify` passed | 2 | Passed; model fixture |

Projects 01–04 were built and checked in the first batch. Projects 05–25 were built and checked in the continuation. Additional HTTP authorization regression tests cover 401, 403, field validation, document ownership and idempotent staff orders. No tests require a paid API.

### Packaged application checks

- Started every project independently. For 05–25, used isolated local ports and an external deterministic HTTP provider fixture; stopped each test JVM afterwards. The fixture is not a language model.
- Verified streaming and cancellation tests, cookie-based memory, advisor rejection, actual tool execution, image attachment, embedding/search flow, document splitting/indexing, RAG abstention, rewritten retrieval, evaluation metrics, usage reporting, retry/deadline behavior and workflow routing.
- Ran the actual lesson 19 MCP server and lesson 20 client together over Streamable HTTP. Tool discovery and a remote price-tool call completed through the real protocol; only the model was a fixture.
- Exercised the capstone through HTTP with Basic authentication, staff ingestion, generated embeddings, memory-store similarity search, owner filtering, chat memory, a real read-only Java tool, typed output conversion and citation checks. Alice could not approve documents or receive Bob's private passage. Unsupported retrieval returned an abstention.
- PostgreSQL-backed projects 12 and 24 used their explicit `memory` profile during packaged checks. This does **not** validate their live database implementation.

### Documentation and configuration

- Checked all local Markdown links, code fences, Java snippets against source, 25 independent POMs/wrappers and concise lesson README lengths.
- Checked PowerShell profile-argument quoting and request files against the handlers. Windows curl/HTTP examples were exercised in equivalent packaged requests.
- `docker compose config --quiet` passed for projects 12, 23 and 24. This validates configuration syntax only. Docker warned that the sandbox could not read the user's Docker settings; no settings were edited.
- `git diff --check` passed. No commit, git push, Docker push, cloud deployment or production secret edit was performed.

## Still needs the local services

- **Real inference remains unverified.** Ollama was not available at localhost:11434. Install/start it and pull `llama3.2:3b` plus `nomic-embed-text`; lesson 10 additionally needs a vision model such as its configured `llava:7b`. Generated wording, semantic quality and model capability need manual review with those actual models.
- **Real PostgreSQL/PGvector and containers remain unverified.** Docker CLI was present, but the Docker daemon was stopped. Start Docker Desktop and follow the relevant README/DEPLOYMENT.md. No image pull, container build/run, persistence-after-restart or database backup/restore was performed.
- Set `LIVE_VECTOR_TESTS=true` before lesson 12's `verify` only when local PostgreSQL and Ollama are ready. That skipped test writes, searches and removes its own temporary test document.
- JDK 21 runtime and macOS/Linux commands were not executed on this Windows/JDK 24 host. The build's Java source/bytecode target is 21.
- Audio/image generation through another provider is outside the local Ollama capability lab. No unsupported API is advertised as implemented and no paid-provider credentials are required.

## Teaching and operational limits

READMEs use the user-approved short format, replacing the original 23-section layout. Keep the problem, familiar example, important code, run command and exercise together in future edits.

The services are classroom examples. Authentication uses public local demo credentials. Memory/rate limits are per process. The capstone serializes turns, clears stale memory on the next request and clears history after document approvals change. Its reindex is delete-then-add, not an atomic index switch. Valid source IDs do not prove factual support. Container tags are teaching defaults, not immutable production pins. Deployment notes explain the identity, TLS, backup, scaling and image-pinning work needed before real deployment.

## Local review

The agent-started demo applications were stopped at the user's request, releasing ports 8081–8084 and 8104. Start the chosen lesson through its IDE or Maven instructions when needed. A capstone **memory-profile** run uses **http://localhost:8104/** and needs live Ollama plus staff ingestion before answering real questions. The persistent Compose setup is documented separately in [24-college-knowledge-assistant/DEPLOYMENT.md](24-college-knowledge-assistant/DEPLOYMENT.md).

Local build logs, test summaries, HTTP-check records and helper scripts are in ignored `.course-build/`; downloaded Maven artifacts are in ignored `.m2-cache/`. Maven Central access needed sandbox elevation. If routine build prompts recur, allow the lesson Maven Wrapper commands and public Maven Central downloads in `~/.Codex/settings.json`; that settings file was not edited.

No required lesson implementation remains. The outstanding items above are live-service and platform validation, plus real-deployment hardening when the examples are adapted beyond local teaching.

## Classroom README additions

Preserved existing content in the root and all 25 lesson READMEs. Added IDE/Postman classroom demonstrations alongside the existing Maven/curl approach, with lesson-specific prerequisites, authentication, indexing, session handling and results to show students. Streaming uses the existing unbuffered curl example for live events; database and MCP lessons explicitly identify their extra services. Every README now includes five interview questions with short answers: 130 Q&As across 26 files.

Documentation checks cover existing-content preservation, entry-point links, methods/URLs against source mappings, sample JSON fields, Markdown code fences and local links. No application code changed and no live services were started for this documentation-only task. The build and runtime results above remain the earlier executed validation, not a claim that the new demonstrations were rerun with real models.
