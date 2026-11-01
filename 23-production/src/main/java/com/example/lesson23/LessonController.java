package com.example.lesson23;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
class LessonController {
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) { client=builder.build(); }
    @PostMapping("/api/chat") Map<String,String> chat(@Valid @RequestBody Question q) {
        return Map.of("answer",client.prompt().user(q.question()).call().content());
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
}

@org.springframework.stereotype.Component("ollama")
class OllamaHealth implements org.springframework.boot.health.contributor.HealthIndicator {
    private final java.net.http.HttpClient http=java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(2)).build();
    private final String baseUrl;
    OllamaHealth(@org.springframework.beans.factory.annotation.Value("${spring.ai.ollama.base-url}") String baseUrl) { this.baseUrl=baseUrl; }
    public org.springframework.boot.health.contributor.Health health() {
        try {
            var request=java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUrl+"/api/tags")).timeout(java.time.Duration.ofSeconds(2)).GET().build();
            int status=http.send(request,java.net.http.HttpResponse.BodyHandlers.discarding()).statusCode();
            return status==200?org.springframework.boot.health.contributor.Health.up().build():org.springframework.boot.health.contributor.Health.down().build();
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); return org.springframework.boot.health.contributor.Health.down().build(); }
        catch(Exception e) { return org.springframework.boot.health.contributor.Health.down().build(); }
    }
}
