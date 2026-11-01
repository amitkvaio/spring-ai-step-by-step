package com.example.lesson06;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import jakarta.servlet.http.*;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

@RestController
class LessonController {
    final ChatMemory memory=MessageWindowChatMemory.builder().maxMessages(6).build();
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) {
        client=builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build()).build();
    }
    @PostMapping("/api/chat") Map<String,String> chat(@Valid @RequestBody Question q, HttpSession session) {
        return Map.of("answer",ask(session.getId(),q.question()));
    }
    // Serialize turns in this small demo so simultaneous requests don't overwrite history.
    synchronized String ask(String id,String question) {
        return client.prompt().user(question).advisors(a -> a.param(ChatMemory.CONVERSATION_ID,id)).call().content();
    }
    @DeleteMapping("/api/chat") void clear(HttpSession session) { memory.clear(session.getId()); }
    @Bean ServletListenerRegistrationBean<HttpSessionListener> expiryCleanup() {
        return new ServletListenerRegistrationBean<>(new HttpSessionListener() {
            @Override public void sessionDestroyed(HttpSessionEvent event) { memory.clear(event.getSession().getId()); }
        });
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
}
