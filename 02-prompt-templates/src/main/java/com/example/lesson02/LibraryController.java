package com.example.lesson02;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/explanations")
class LibraryController {
    private final ChatClient chatClient;

    LibraryController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are a librarian. Explain ideas in simple English. Do not invent library policies.")
                .build();
    }

    @PostMapping
    LibraryReply explain(@Valid @RequestBody LibraryQuestion request) {
        String answer = chatClient.prompt()
                // Named parameters keep the reusable wording in one place.
                // Templates format text; they do not make untrusted text safe.
                .user(user -> user.text("Explain {topic} to a reader named {name}. Use a library example.")
                        .param("topic", request.topic()).param("name", request.name()))
                .call()
                .content();
        return new LibraryReply(answer);
    }

    record LibraryQuestion(@NotBlank @Size(max = 80) String name,
                           @NotBlank @Size(max = 200) String topic) {}
    record LibraryReply(String answer) {}
}
