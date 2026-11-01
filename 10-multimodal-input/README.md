# 10 - Multimodal input (optional)

> **Lesson 10** · **Local port: 8090** · **Run with your IDE or Maven**

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it) · [Send a request](#try-it)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- A librarian receives a book-cover photo.
- A text-only question cannot show the model what is in that photo.

- Multimodal means using more than one kind of input.
- Here the user message contains a question and a PNG image.
- The model must support image input.

## The important code

- Open [LessonController.java](src/main/java/com/example/lesson10/LessonController.java).
- The key part is:

```java
client.prompt().user(u -> u.text(cover.question())
    .media(MimeTypeUtils.IMAGE_PNG,new ByteArrayResource(image))).call().content()
```

- `describe` decodes the supplied base64 image and checks its PNG signature and size.
- `media(...)` attaches the bytes to the user message.
- No user-supplied file path or remote URL is fetched.

## Run it

- Use the [one-time setup](../README.md#set-up-once).
- Open a terminal inside **10-multimodal-input**.
- Each project has its own POM and wrapper.

- Pull a vision model first: `ollama pull llava:7b`.
- This lesson defaults to that model, unlike the text lessons.
- The supplied one-pixel PNG checks the request format; replace it with a real cover photo for a useful description.

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
- Open **http://localhost:8090** for a small status response.
- Stop with Ctrl+C before rebuilding.
- Configuration is in [application.properties](src/main/resources/application.properties).

## Try it

[request.json](request.json) contains:

```json
{
  "question": "Describe this image.",
  "imageBase64": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/lXcAAAAASUVORK5CYII="
}
```

From another terminal in the same folder:

```powershell
curl.exe -i http://localhost:8090/api/describe -H "Content-Type: application/json" --data-binary "@request.json"
```

- On macOS/Linux, replace `curl.exe` with `curl`.
- Example response shape; AI wording and similarity scores can vary:

```json
{
  "answer": "This is a small test image."
}
```

## Classroom demo

- Open this project's `pom.xml` in your IDE, select a compatible JDK, and run [LessonApplication.main](src/main/java/com/example/lesson10/LessonApplication.java).
- Or use the existing **Maven + curl** commands above.
- Follow the lesson's prerequisite setup first.
- Wait for `Started LessonApplication` and run one instance of this project at a time.

For a **Postman** demonstration, create a request with these settings:

| Setting | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8090/api/describe` |
| Body | **raw → JSON**; copy the existing [request.json](request.json) |
| Header | `Content-Type: application/json` |
| Authorization | None for this local lesson |

- Follow the sequence below before sending the main request.
- Click **Send** and display the response body and status beside the source code.
- Browser GET requests can show the status or documented GET endpoints; POST endpoints need a request body.

1. Before class, pull the vision model and set the IDE run environment to `OLLAMA_CHAT_MODEL=llava:7b`.
   - Start this lesson alone if local memory is tight.
2. Send the supplied `request.json` to demonstrate the JSON/base64 format.
   - It contains a one-pixel PNG; it is not a meaningful book cover.
3. For the actual class example, replace `imageBase64` with a small book-cover PNG encoded as plain base64, then ask for its visible title.
   - Compare the answer with the picture.
   - Try `imageBase64=aGVsbG8=` to show **400** for a non-PNG.

- To encode your own PNG in PowerShell, replace this example path with the actual image path, then copy the printed text into the JSON field.
- Keep the image under 1 MB:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('D:\Classroom\cover.png'))
```

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What makes this a multimodal request?**

- One user message contains text and an image.
- The selected model must support the media type, not merely ordinary text chat.

**Q2. Can llama3.2:3b read the book-cover image?**

- That model is a text model.
- This lesson configures a separate vision model; check the actual selected model before the demonstration.

**Q3. Does base64 encrypt an image?**

- No; it encodes bytes as text for transport.
- It provides no confidentiality by itself.

**Q4. What does the PNG signature check prove?**

- Only that the initial bytes match the expected format and the size is within this limit.
- It is not full image decoding, malware scanning or OCR accuracy validation.

**Q5. Why accept bytes instead of a caller-supplied URL?**

- It avoids making the server fetch arbitrary remote resources in this lesson.
- Remote fetching would require additional network and SSRF controls.

## A few things to remember

- Only PNG inputs under 1 MB are accepted.
- A signature check is a basic format guard, not full image validation.
- Image support is model-specific.
- A text-only model is not a substitute.
- The tests check image attachment and invalid input; they do not assess image understanding.

- [Tests](src/test/java/com/example/lesson10/LessonTest.java) run without paid APIs or a real model.
- See [PROGRESS.md](../PROGRESS.md) for what was actually verified.

## Try a small change

- Use a book-cover PNG and ask for its visible title.
- Compare the answer with the actual cover instead of assuming it is correct.

Next: [Embeddings and semantic search](../11-embeddings-semantic-search/README.md).

- Official reference: [Spring AI - Multimodal input (optional)](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
