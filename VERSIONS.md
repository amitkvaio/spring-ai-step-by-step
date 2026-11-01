# Version baseline

Baseline verified on **2026-11-01** against official Spring documentation, release listings and Maven Central metadata. Remaining lesson APIs were checked against the same 2.0.2 documentation and downloaded artifacts on **2026-11-02**.

| Component | Selection | Reason |
|---|---|---|
| Spring AI | 2.0.2 | Latest stable release verified; 2.1.0-RC1 is a preview and is excluded |
| Spring Boot | 4.1.2 | Stable release supported by Spring AI 2.0.x |
| Java source target | 21 | LTS language baseline; no preview features |
| Validation JDK | Oracle JDK 24.0.2 | Installed locally; compatible with Boot 4.1.2; compiles with release 21 |
| Maven | 3.9.9 | Pinned by each project's Maven Wrapper; Boot requires at least 3.6.3 |
| Maven Wrapper plugin | 3.3.4 | Generates standard script-only wrappers; no wrapper JAR required |
| Provider | Local Ollama | No cloud account or paid API key for the main path |
| Chat model | `llama3.2:3b` | Configurable teaching default, not a guarantee of output quality |
| Embedding model | `nomic-embed-text` | Separate local embedding model; persistent schema uses 768 dimensions |
| Optional vision model | `llava:7b` | Lesson 10 default; image understanding remains unverified without a model download |
| MCP SDK | 2.1.0 | Resolved transitively by the Spring AI 2.0.2 MCP starters; distinct from the Spring AI version |
| Reactor | 3.8.8 | Resolved through Boot; `reactor-test` matches it for streaming tests |
| PostgreSQL/PGvector image | `pgvector/pgvector:pg17` | Local Compose example; tag is mutable and runtime was not executed here |

Every POM imports `org.springframework.ai:spring-ai-bom:2.0.2` and uses the Boot parent directly. There is no repository parent POM and no sibling source dependency. Maven Central is sufficient; no milestone or snapshot repository is configured.

## API details checked during implementation

- Use `OllamaChatOptions` for chat. Older `OllamaOptions` examples do not compile against this baseline.
- `ChatClient` request `.options(...)` accepts an options **builder** in 2.0.2. Do not pass the result of `.build()` to that method. The runtime provider-body test checks that the default model and per-request settings are combined.
- In the 2.0.2 retry auto-configuration, `spring.ai.retry.max-attempts` is passed to Spring Framework's `maxRetries`. Despite the property's name, **0 means one initial call with no retries** in this pinned version. The beginner projects select 0 and test the actual provider-call count on HTTP 503. Recheck this behavior when upgrading rather than copying the value blindly.
- `.call()` selects the synchronous path; the terminal operation (`.content()` or `.entity(...)`) performs the request. The basic `.entity(...)` mapping is not provider-native schema enforcement. Generated fields are validated separately in lesson 04.

Ollama and model artifacts are installed separately. Model tags may change upstream. Record `ollama --version` and `ollama list` when reproducing a live classroom run. Live model/version checks have not yet been performed here. No claim is made that the chosen model is the latest or supports every advanced capability.

## Sources

- [Spring AI releases](https://github.com/spring-projects/spring-ai/releases): 2.0.2 stable; 2.1.0-RC1 prerelease.
- [Spring AI getting started](https://docs.spring.io/spring-ai/reference/getting-started.html): 2.0.x supports Boot 4.0.x and 4.1.x.
- [Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html): Boot 4.1.2 supports Java 17–27 and Maven 3.6.3+.
- [Spring AI BOM metadata](https://repo.maven.apache.org/maven2/org/springframework/ai/spring-ai-bom/maven-metadata.xml): artifact versions independently checked. The `release` field can name a release candidate, so it is not by itself proof of stability.
- [Boot parent 4.1.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/4.1.2/spring-boot-starter-parent-4.1.2.pom): artifact availability verified.
- [Ollama integration](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).

Container examples also use `eclipse-temurin:21-jre` and `ollama/ollama:latest`. These are teaching tags, not immutable deployment pins. Resolve and record approved image digests before a real deployment. Docker Compose syntax checks do not prove that images were pulled or containers ran.

See [PROGRESS.md](PROGRESS.md) for build and runtime evidence, distinct from documentation checks.
