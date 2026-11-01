# 01 - First AI chat

> **Lesson 01** · **Local port: 8081** · **Run with your IDE or Maven**

Let's start with one small goal: send a question from Java and get an AI reply.

## Quick navigation

- **Learn:** [The problem](#what-problem-are-we-solving) · [Important code](#the-important-code)
- **Run:** [Start locally](#run-it-and-send-a-question) · [Install Ollama on D:](#install-ollama-on-d-windows)
- **Teach:** [Classroom demo](#classroom-demo) · [Interview FAQ](#interview-faq) · [Exercise](#try-a-small-change)

## What problem are we solving?

- Imagine a student asks a college help desk, "What does a help desk do?"
- We want our application to draft a simple answer.

- Here, Ollama runs the language model.
- Spring AI lets our Java code talk to it.
- The model can draft text, but it has no access to your college's actual records.

## The important code

Open [ChatController.java](src/main/java/com/example/lesson01/ChatController.java), method `chat`:

```java
String answer = chatClient.prompt()
        .user(request.question())
        .call()
        .content();
```

- `prompt()` starts a new request.
- `user(...)` adds the student's question.
- `call()` selects a complete response rather than a stream.
- `content()` makes the call and gives us the reply text.

- The constructor builds `ChatClient` from Spring's configured builder.
- The Ollama starter supplies the model adapter underneath it.
- We return the text in `ChatReply`, so the response has an `answer` field.

For our sample request, the path is:

```text
POST /api/chat -> ChatController.chat -> user question -> Ollama -> ChatReply
```

## Install Ollama on D: (Windows)

- You'll need JDK 21 or a compatible newer JDK.
- A computer with 16 GB RAM is a reasonable starting point for this small model and Spring Boot.
- Local model use needs no cloud API key.

- Ollama's application and model downloads have separate locations.
- Set both to D: before downloading the model.

1. **Create the folders.**
   - In File Explorer, create:

   ```text
   D:\Ollama\Downloads
   D:\Ollama\Models
   ```

2. **Download the installer.**
   - Open the [official Windows download page](https://ollama.com/download/windows) and choose **Download manually**.
   - Save `OllamaSetup.exe` in `D:\Ollama\Downloads`.
   - Don't double-click it yet.

3. **Choose where models are stored.**
   - Search Windows Start for **Edit environment variables for your account**.
   - Under **User variables**, click **New** (or edit the variable if it already exists):

   | Field | Value |
   |---|---|
   | Variable name | `OLLAMA_MODELS` |
   | Variable value | `D:\Ollama\Models` |

   Click **OK** to save and close the dialogs.

4. **Install the application on D:.**
   - Open a new PowerShell window and run:

   ```powershell
   & "D:\Ollama\Downloads\OllamaSetup.exe" /DIR="D:\Ollama\App"
   ```

   - Complete the installer.
   - The `/DIR` option selects the application folder.
   - If Ollama was already running when you changed the environment variable, quit its system-tray application and reopen it.
   - These location settings follow the [official Windows instructions](https://docs.ollama.com/windows).

5. **Check the installation.**
   - Open another new PowerShell window:

   ```powershell
   ollama --version
   $env:OLLAMA_MODELS
   ```

   - You should see an Ollama version and `D:\Ollama\Models`.
   - If `ollama` isn't recognized, check the installed executable directly:

   ```powershell
   & "D:\Ollama\App\ollama.exe" --version
   ```

   If the full path works, add `D:\Ollama\App` to your user `Path` variable and open a new terminal.

6. **Download the model.**
   - Open Ollama from the Start menu, then run:

   ```powershell
   ollama pull llama3.2:3b
   ollama list
   ```

   - Wait for the download to finish.
   - The list should contain `llama3.2:3b`, and its files should be under `D:\Ollama\Models`.
   - The [model download](https://ollama.com/library/llama3.2:3b) is about 2 GB; leave extra disk space for Ollama itself.
   - Some logs, updates and temporary files may still use C:.

7. **Try the model and local API.**

   ```powershell
   ollama run llama3.2:3b "Explain Spring Boot in two simple sentences."
   curl.exe http://localhost:11434/api/tags
   ```

   - The first command should produce an answer.
   - The second should return JSON listing the installed model.

- Your project already points to `http://localhost:11434` and model `llama3.2:3b`.
- No configuration changes are needed for this setup.

## Start, run and stop Ollama (Windows)

1. **Start Ollama.**
   - Open **Ollama** from the Windows Start menu.
   - Alternatively, run this in PowerShell and leave that terminal open:

   ```powershell
   ollama serve
   ```

   If port 11434 is already in use by Ollama, use that running instance.

2. **Try the model.**
   - In another terminal, run:

   ```powershell
   ollama run llama3.2:3b "Say hello"
   ```

   - You don't need to run this command before every Spring AI request.
   - The application loads the downloaded model automatically when it sends a question to Ollama.

3. **Unload the model and free its memory.**

   ```powershell
   ollama stop llama3.2:3b
   ollama ps
   ```

   - `ollama ps` lists models still loaded in memory.
   - Stopping a model keeps Ollama running and preserves its downloaded files.
   - Another question can load it again.

4. **Stop Ollama completely.**
   - If you opened the Windows app, right-click its system-tray icon and choose **Quit Ollama**.
   - If you started `ollama serve`, press **Ctrl+C** in that terminal instead.

## Run it and send a question

- Starting the application and sending a question are two separate steps.
- Choose either approach below.
- Keep Ollama running in both cases.

### Approach 1: Run from your IDE and use Postman

1. Open **01-first-ai-chat** in IntelliJ IDEA, Eclipse or your preferred IDE.
   - Import its `pom.xml` as a Maven project and let the dependencies load.
2. Select JDK 21 or a compatible newer JDK for the project.
3. Open [LessonApplication.java](src/main/java/com/example/lesson01/LessonApplication.java) and run its `main` method.
4. Wait for `Started LessonApplication` in the IDE console.
   - The application listens at **http://localhost:8081**.
5. Open Postman and create a request:

   | Setting | Value |
   |---|---|
   | Method | `POST` |
   | URL | `http://localhost:8081/api/chat` |
   | Body | **raw**, then select **JSON** |

   Paste this body:

   ```json
   {"question":"Explain what a college help desk does."}
   ```

6. Check that `Content-Type` is `application/json`, then click **Send**.
   - Read the answer in Postman's response panel.

- Use the IDE's **Stop** button to stop the application.
- You don't need curl for this approach.
- The project is a REST API, so opening its URL in a browser does not provide a question form.

### Approach 2: Run with Maven and use curl

- After the Windows setup above, open PowerShell and run the following.
- On macOS/Linux, follow the [one-time setup](../README.md#set-up-once), then enter **01-first-ai-chat**.

**Windows PowerShell**:

```powershell
cd D:\git\spring-ai-step-by-step\01-first-ai-chat
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

**macOS/Linux**:

```bash
sh ./mvnw clean verify
sh ./mvnw spring-boot:run
```

- Wait for `Started LessonApplication`.
- The app runs at **http://localhost:8081**.
- Stop it with Ctrl+C before rebuilding.

[request.json](request.json) contains:

```json
{"question":"Explain what a college help desk does."}
```

In another terminal, inside this same folder:

```powershell
curl.exe -i http://localhost:8081/api/chat -H "Content-Type: application/json" --data-binary "@request.json"
```

On macOS/Linux, use `curl` instead of `curl.exe`.

- Both approaches use port 8081.
- Stop the current application before switching approaches; otherwise you'll get "Port 8081 was already in use."

### Expected reply for either approach

- A successful request returns HTTP **200** and a reply like this.
- The AI wording can change:

```json
{"answer":"A college help desk helps students find information and contact the right department."}
```

## Classroom demo

- The two complete run approaches are already shown above.
- Use this short sequence when presenting the lesson.

1. Use either **IDE + Postman** or **Maven + curl** from the instructions above.
   - Show `POST http://localhost:8081/api/chat` with the question in `request.json`.
2. Explain the path while the request runs: controller → ChatClient → local Ollama → JSON reply.
   - Students should see HTTP **200** and an `answer` field; the sentence itself can vary.
3. Replace the question with an empty string and send again.
   - Show HTTP **400** and explain that invalid input is rejected before a model call.

- Stop the Java application with the IDE's **Stop** button or Ctrl+C in its Maven terminal before restarting.
- Ports and sample response formats are documented above.
- Generated wording and similarity scores can vary.

- For a demonstration without live services, run the project's documented `clean verify` command and show the topic's tests.
- Clearly call this a **deterministic test demonstration**, not a live model response.
- See the [classroom setup](../README.md#classroom-setup) and [validation record](../PROGRESS.md).

## Interview FAQ

**Q1. What is the difference between ChatClient and ChatModel?**

- ChatClient is the fluent request API.
- ChatModel is the lower-level provider abstraction that performs generation; the Ollama starter supplies its implementation.

**Q2. What do call() and content() do?**

- call() selects the synchronous response path.
- The terminal content() operation invokes the model and extracts its text.

**Q3. Why does this application forget the previous question?**

- Each request contains only its current question.
- Earlier messages must be explicitly included, for example through a chat-memory advisor.

**Q4. Why return 400 for a blank question and 502 for a provider failure?**

- 400 means the caller supplied invalid input.
- 502 means the application could not obtain a usable response from its upstream model service.

**Q5. Can the model confirm the current admission deadline?**

- This application supplies no current college records.
- A plausible generated date is not verification; use approved data through retrieval or a controlled tool.

## A few things to remember

- Each request starts fresh.
- This app doesn't remember earlier questions.
- Blank questions or questions over 1,000 characters return **400**.
- If Ollama is unavailable, the app returns **502**.
- Check `ollama list` and the model service.
- The [HTTP tests](src/test/java/com/example/lesson01/LessonHttpTest.java) use a local fixture.
- They check the request, reply and errors without calling a real model.

## Try a small change

- Ask how a help desk can help a new student.
- The answer should change, but the JSON should still have an `answer` field.

**Why can't it confirm today's admission deadline?**

- We haven't given it approved college information.
- We'll solve that later with document retrieval.

**Is Spring AI the model?**

No; it connects our code to the model running in Ollama.

- Next: [02 - Prompt templates](../02-prompt-templates/README.md).
- We'll reuse instructions instead of writing a fresh prompt each time.

- Official references: [ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html), [Ollama](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).
- Use the [2.0.2 baseline](../VERSIONS.md).
