package com.example.lesson03;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

// Real HTTP -> Boot validation -> real ChatClient -> Ollama adapter -> local fixture.
// No LLM inference, external service, model download or API charge is involved.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LessonHttpTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final AtomicReference<String> modelText = new AtomicReference<>();
    private static final AtomicReference<String> providerRequest = new AtomicReference<>();
    private static final AtomicInteger providerStatus = new AtomicInteger(200);
    private static final AtomicInteger calls = new AtomicInteger();
    private static final HttpServer PROVIDER = startProvider();
    private static final String VALID_REQUEST = """
            {"question":"Why does ice melt?","temperature":0.2,"maxTokens":96}
            """;

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void configureProvider(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.ollama.base-url",
                () -> "http://127.0.0.1:" + PROVIDER.getAddress().getPort());
    }

    @BeforeEach
    void resetProvider() {
        modelText.set("""
                A useful teaching reply.
                """.strip());
        calls.set(0);
        providerStatus.set(200);
        providerRequest.set(null);
    }

    @Test
    void sendsExpectedPromptAndReturnsResponse() throws Exception {
        var reply = post(VALID_REQUEST);
        assertThat(reply.statusCode()).isEqualTo(200);
        assertThat(calls.get()).isEqualTo(1);
        var sent = JSON.readTree(providerRequest.get());
        assertThat(sent.get("model").asText()).isEqualTo("llama3.2:3b");
        var messages = sent.get("messages");
        assertThat(sent.get("options").get("temperature").asDouble()).isEqualTo(0.2);
        assertThat(sent.get("options").get("num_predict").asInt()).isEqualTo(96);
        assertThat(JSON.readTree(reply.body()).get("answer").asText()).isEqualTo("A useful teaching reply.");
    }

    @Test
    void rejectsInvalidInputBeforeCallingProvider() throws Exception {
        var reply = post("""
                {"question":"Ice?","temperature":2.0,"maxTokens":96}
                """);
        assertThat(reply.statusCode()).isEqualTo(400);
        assertThat(calls.get()).isZero();
    }

    @Test
    void rejectsOversizedInputBeforeCallingProvider() throws Exception {
        String input = VALID_REQUEST.replace("Why does ice melt?", "x".repeat(1001));
        assertThat(post(input).statusCode()).isEqualTo(400);
        assertThat(calls.get()).isZero();
    }

    @Test
    void returnsSafeErrorWhenProviderFails() throws Exception {
        providerStatus.set(503);
        modelText.set("private provider diagnostic");
        var reply = post(VALID_REQUEST);
        assertThat(reply.statusCode()).isEqualTo(502);
        assertThat(reply.body()).doesNotContain("private provider diagnostic");
        assertThat(calls.get()).isEqualTo(1);
    }

    private HttpResponse<String> post(String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/answers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        try (var client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    private static HttpServer startProvider() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/chat", exchange -> {
                calls.incrementAndGet();
                providerRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] response = JSON.writeValueAsBytes(Map.of(
                        "model", "llama3.2:3b", "done", true,
                        "message", Map.of("role", "assistant", "content", modelText.get())));
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(providerStatus.get(), response.length);
                try (var output = exchange.getResponseBody()) { output.write(response); }
            });
            server.start();
            return server;
        }
        catch (IOException exception) { throw new ExceptionInInitializerError(exception); }
    }

    @AfterAll
    static void stopProvider() { PROVIDER.stop(0); }
}
