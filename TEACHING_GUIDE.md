# Teaching guide

## Teaching routine

Use the nine steps in every classroom demonstration: problem → real-life example → concept → application mapping → smallest code → important code explanation → request trace → response shape → exercise. Explain new AI words as they appear. Students already know Java and basic Spring Boot; do not spend the lesson rebuilding that background.

Start with the vocabulary in the [course introduction](README.md#before-lesson-01). Ask why a generated college answer might be wrong. Explain that a model has learned patterns and has no automatic connection to today's college records. The clerk analogy introduces drafting; it does not imply a model understands or verifies facts as a person would.

Before class, build the specific project, download the model, and try the exact request in its README. Keep a copy of expected response shapes; wording varies. Do not present the deterministic test fixture as a language model. Pause live demonstrations when machines cannot load the model, and use the prompt assertions in the tests to inspect the application path instead.

## First batch: classroom demonstrations

| Lesson | Demonstration | Small exercise | Checkpoint before moving on |
|---|---|---|---|
| 01 | Ask what a college help desk does. Trace `ChatController.chat` through `prompt`, `user`, `call`, `content`. Submit a blank question and observe 400 before a provider call. | Change the question to another general college topic. | Student distinguishes `ChatClient`, provider adapter and actual model; explains why this app cannot verify opening hours. |
| 02 | Write a librarian's instruction card with blanks for reader and topic. Change `name` and `topic` independently. Inspect the two message roles in `LessonHttpTest`. | Add a validated reading-level parameter to the template. | Student can predict rendered text and explain why a template does not prevent prompt injection. |
| 03 | Ask a teacher for a short explanation. Keep the question fixed and compare temperatures 0.0 and 0.8 over several runs; then lower the output budget. | Compare 32 and 128 output tokens. | Student explains that token limits are not word limits and temperature is not a truthfulness setting. Repeated outputs may still match. |
| 04 | Put a support issue into a fixed ticket form. Show the `Ticket` record, `.entity`, and the separate validator check. Run malformed/missing-output tests. | Add a nonblank department field and update the fixture and assertions. | Student distinguishes parsing, field validation and correctness of the classification. |

Suggested pacing: one session per beginner project, with a short vocabulary review, the example, a live trace, an exercise and a checkpoint. Adjust to student experience rather than promising fixed timings.

## Remaining lessons

- **05–10:** Begin with displaying a long answer gradually. Then show why separate HTTP calls forget prior messages, why selected history needs a bound, and how advisors process requests. Introduce the shop price-list example for tools. The model requests an action; Java performs it after validation. Compare an allowed read with an unauthorized write. Use images only after checking model support.
- **11–16:** Introduce meaning-based lookup with two differently worded library questions. Compute embeddings, then persist them with metadata. Split approved handbook pages and retain page/source identifiers. Retrieve before generating a reply. Grade retrieval separately from the final answer. A citation is useful only if its source supports the claim.
- **17–18:** Observe a slow or failed model call. Measure first, then introduce bounded timeouts and retries. Explain that token usage can be missing and local models still consume hardware resources. Do not retry a side-effecting tool as if it were a harmless text request.
- **19–20:** Show a controlled shop service counter. Teach the MCP server before the client. Distinguish a protocol for exposing tools from authority to execute them. Confirm the pinned transport APIs before writing code.
- **21–23:** Use a college record that only its owner may read. Enforce access outside the prompt. Add a hostile retrieved passage and verify it cannot grant tool rights. Route workflows explicitly and bound loops. Demonstrate container startup, readiness and recovery with no production credentials.
- **24:** Build the college help desk only after the required prerequisites. Start from approved documents and add features one at a time. Explain the purpose of each feature and link back to its lesson.

## Capstone acceptance checkpoints

| Feature | Problem it solves | Evidence students must show |
|---|---|---|
| Ingestion and persistent vector index | Approved handbook pages must be searchable | Reindex does not duplicate passages; source metadata survives |
| RAG with source references | Answers need supporting passages | Cited passages support the answer; unsupported question gets an abstention |
| Bounded, isolated memory | Follow-up questions need selected earlier context | Two authenticated students cannot read each other's history |
| Read-only opening-hours tool | Current application data is absent from documents | Application validates arguments and returns authorized results |
| Structured answer | UI needs answer and source fields | Parse and field validation tests pass; generated facts still reviewed |
| Input and access controls | Untrusted inputs and documents can request unsafe actions | Unauthorized retrieval and tool calls fail regardless of prompt text |
| Error handling and metrics | Operators need bounded failures and useful evidence | Timeout path, safe error reply and latency/failure measurements demonstrated |
| Deployment instructions | Students need repeatable startup and cleanup | Independent build plus Compose startup and documented URLs actually checked |

## Assessment and feedback

Use each README's exercises, interview questions and FAQs. Ask students to explain an input-to-output trace using actual class names. Separate suggested solutions from the task so students can attempt it first. Review exact generated wording only as an example; never grade by requiring that wording. For later model-based evaluation, record the model, prompt set and rubric, and keep paid judge calls opt-in.

All 25 projects now have source code, tests and short READMEs. Use PROGRESS.md to distinguish executed demonstrations from checks that still need real Ollama models or PostgreSQL.
