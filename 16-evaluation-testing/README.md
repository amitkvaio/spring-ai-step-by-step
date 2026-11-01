# 16 - Evaluation and testing

> **Lesson 16** · **Local port: 8096** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- A readable answer is not enough.
- We need to check whether retrieval found the right pages and whether references name real sources.

- A teacher uses separate marks for different skills.
- Precision measures how much retrieved material was relevant.
- Recall measures how much of the expected material was found.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson16/LessonController.java).
- The key part is:

```java
new Report(retrieved.isEmpty()?0:(double)correct/retrieved.size(),expected.isEmpty()?1:(double)correct/expected.size())
```

- `evaluate` compares source IDs against a labeled case.
- `CitationEvaluator` implements Spring AI's `Evaluator` and checks that cited IDs exist in the supplied documents.
- An optional `/api/judge` asks a model to assess support, but that judge can also be wrong.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **16-evaluation-testing**.
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
- Open **http://localhost:8096** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "expected": [
    "library",
    "borrowing"
  ],
  "retrieved": [
    "library"
  ]
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8096/api/evaluate -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response:

```json
{
  "precision": 1.0,
  "recall": 0.5
}
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson16/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8096/api/evaluate` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Send `request.json`.
   - Show exact deterministic results: precision **1.0** and recall **0.5**.
   - This endpoint needs no live model.
2. Add `fees` to `retrieved`; precision becomes **0.5** while recall stays **0.5**.
   - Open the citation tests to compare a known and invented source ID.
3. Keep `/api/judge` disabled for the basic demonstration.
   - If explicitly enabling it through `JUDGE_ENABLED=true`, explain that its model-generated judgment is an additional fallible assessment.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What are retrieval precision and recall?**

- Precision is the fraction of retrieved IDs that are relevant.
- Recall is the fraction of expected relevant IDs that retrieval found.

**Q2. Why avoid assertions about exact generated wording?**

- Equivalent correct answers can use different words.
- Test deterministic contracts separately and evaluate factual support with a suitable rubric.

**Q3. Can an LLM judge be wrong?**

- Yes; the judge is another model with its own limitations.
- Use labeled examples, review disagreements and keep judge calls opt-in.

**Q4. What does CitationEvaluator verify here?**

- That the answer contains citations and every cited ID exists in the supplied sources.
- It does not prove that those sources support the claims.

**Q5. What makes a useful evaluation dataset?**

- Representative questions with expected evidence and clear grading rules, including unsupported and adversarial cases.
- Review changes against the same baseline cases.

## A few things to remember

- The default evaluation endpoint makes no model calls.
- Exact generated wording is never a test assertion.
- Model judging is off by default.
- Set `JUDGE_ENABLED=true` and submit `judge-request.json` to `/api/judge` to opt in.
- Local Ollama still uses compute.
- A valid citation ID is not proof that its passage supports the answer.
- Human-labeled cases and manual review still matter.

- [Tests](src/test/java/com/example/lesson16/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Add an irrelevant source to `retrieved`.
- Precision should fall while recall stays the same.

Next: [Observability, usage and cost](../17-observability/README.md).

- Official reference: [Spring AI - Evaluation and testing](https://docs.spring.io/spring-ai/reference/api/testing.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
