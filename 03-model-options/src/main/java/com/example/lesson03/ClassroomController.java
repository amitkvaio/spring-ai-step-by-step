package com.example.lesson03;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
class ClassroomController {
    private final ChatClient chatClient;

    ClassroomController(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem("You are a classroom tutor. Give one short, simple explanation.")
                .build();
    }

    @PostMapping
    ClassroomReply answer(@Valid @RequestBody ClassroomQuestion request) {
        // numPredict is Ollama's output-token budget, not a word or character limit.
        // Spring AI 2.0's request options method accepts a builder.
        var options = OllamaChatOptions.builder()
                .temperature(request.temperature())
                .numPredict(request.maxTokens());
        String answer = chatClient.prompt().user(request.question())
                .options(options).call().content();
        return new ClassroomReply(answer);
    }

    record ClassroomQuestion(@NotBlank @Size(max = 1000) String question,
                             @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double temperature,
                             @NotNull @Min(16) @Max(512) Integer maxTokens) {}
    record ClassroomReply(String answer) {}
}
