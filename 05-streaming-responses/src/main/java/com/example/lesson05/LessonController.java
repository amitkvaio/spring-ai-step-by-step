package com.example.lesson05;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import org.springframework.http.codec.ServerSentEvent;
import java.time.Duration;

@RestController
class LessonController {
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) { client=builder.build(); }
    @PostMapping(value="/api/stream", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ServerSentEvent<String>> stream(@Valid @RequestBody Question request) {
        // MVC subscribes to the Flux; disconnecting cancels upstream work.
        return client.prompt().user(request.question()).stream().content()
                .timeout(Duration.ofSeconds(45))
                .map(text -> ServerSentEvent.builder(text).event("token").build())
                .concatWithValues(ServerSentEvent.builder("complete").event("done").build())
                // Headers may already be sent: signal an SSE error, not a new HTTP status.
                .onErrorResume(error -> Flux.just(ServerSentEvent.builder("The model stream failed.").event("error").build()));
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
}
