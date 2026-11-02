package com.example.lesson01;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
class ChatController {
    private final ChatClient chatClient;

    ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping
    ChatReply chat(@Valid @RequestBody ChatQuestion request) {
        // Each request stands alone. No conversation memory is configured here.
        String answer = chatClient.prompt()
                .user(request.getQuestion()) // The user's question
                .call()  // Send it and wait for a response
                .content(); // Extract the response text
        return new ChatReply(answer);
    }

    //record ChatQuestion(@NotBlank @Size(max = 1000) String question) {}
    //record ChatReply(String answer) {}
}
