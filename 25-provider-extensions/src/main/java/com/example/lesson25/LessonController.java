package com.example.lesson25;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;

@RestController
class LessonController {
    private final ChatClient client; private final String comparisonModel;
    LessonController(ChatClient.Builder builder,@Value("${course.comparison-model}") String comparisonModel) { client=builder.build(); this.comparisonModel=comparisonModel; }
    @GetMapping("/api/capabilities") Map<String,String> capabilities() {
        return Map.of("provider","local Ollama","text","supported","imageInput","requires a vision model; see lesson 10",
            "tools","requires a tool-capable model; see lesson 08","audioGeneration","not implemented by this Ollama integration",
            "imageGeneration","not implemented by this Ollama integration");
    }
    @PostMapping("/api/compare") Comparison compare(@Valid @RequestBody Question q) {
        String first=client.prompt().user(q.question()).call().content();
        String second=client.prompt().user(q.question()).options(OllamaChatOptions.builder().model(comparisonModel)).call().content();
        return new Comparison(first,comparisonModel,second);
    }
    @PostMapping("/api/native-json") Recommendation nativeJson(@Valid @RequestBody Question q) {
        // Unlike lesson 04, pass the schema through the provider's structured-output option.
        return client.prompt().user(q.question()).call().entity(Recommendation.class,spec -> spec.useProviderStructuredOutput());
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
    record Comparison(String defaultAnswer,String comparisonModel,String comparisonAnswer) {}
    public record Recommendation(String title,String reason) {}
}
