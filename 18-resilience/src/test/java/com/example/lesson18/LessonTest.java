package com.example.lesson18;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;
class LessonTest {

    static ChatModel model(String text) {
        ChatModel model=mock(ChatModel.class);
        when(model.getOptions()).thenReturn(OllamaChatOptions.builder().build());
        when(model.call(any(Prompt.class))).thenReturn(reply(text));
        return model;
    }
    static ChatResponse reply(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    @Test void deadlineReleasesConcurrencySlot() {
        var model=model("unused"); when(model.stream(any(Prompt.class))).thenReturn(Flux.never());
        var c=new LessonController(ChatClient.builder(model));
        StepVerifier.withVirtualTime(() -> c.chat(new LessonController.Question("Hi")))
            .thenAwait(Duration.ofSeconds(6)).expectErrorMatches(e -> e instanceof org.springframework.web.server.ResponseStatusException status && status.getStatusCode().value()==504).verify();
        assertThat(c.slots.availablePermits()).isEqualTo(2);
    }
    @Test void retriesOnlyOnceForTransientReadFailure() {
        var count=new AtomicInteger(); var model=model("unused");
        when(model.stream(any(Prompt.class))).thenReturn(Flux.defer(() -> count.incrementAndGet()==1
            ?Flux.error(new org.springframework.ai.retry.TransientAiException("temporary")):Flux.just(reply("Hi"))));
        StepVerifier.withVirtualTime(() -> new LessonController(ChatClient.builder(model)).chat(new LessonController.Question("Hi")))
            .thenAwait(Duration.ofSeconds(2)).assertNext(r -> assertThat(r).containsEntry("answer","Hi")).verifyComplete();
        assertThat(count).hasValue(2);
    }
    @Test void localRateLimitRejectsEleventhRequest() {
        var gate=new RateGate(Clock.fixed(Instant.EPOCH,ZoneOffset.UTC));
        for(int i=0;i<10;i++) assertThat(gate.allow()).isTrue();
        assertThat(gate.allow()).isFalse();
    }
}
