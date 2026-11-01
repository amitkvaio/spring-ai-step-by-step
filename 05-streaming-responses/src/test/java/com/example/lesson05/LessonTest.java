package com.example.lesson05;

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
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Test void streamsChunksThenCompletion() {
        var model=model("unused");
        when(model.stream(any(Prompt.class))).thenReturn(Flux.just(reply("Hello "),reply("student")));
        var controller=new LessonController(ChatClient.builder(model));
        StepVerifier.create(controller.stream(new LessonController.Question("Hi")))
            .assertNext(e -> { assertThat(e.event()).isEqualTo("token"); assertThat(e.data()).isEqualTo("Hello "); })
            .assertNext(e -> assertThat(e.data()).isEqualTo("student"))
            .assertNext(e -> assertThat(e.event()).isEqualTo("done")).verifyComplete();
    }
    @Test void convertsFailureToAnErrorEvent() {
        var model=model("unused");
        when(model.stream(any(Prompt.class))).thenReturn(Flux.error(new IllegalStateException("private diagnostic")));
        StepVerifier.create(new LessonController(ChatClient.builder(model)).stream(new LessonController.Question("Hi")))
            .assertNext(e -> { assertThat(e.event()).isEqualTo("error"); assertThat(e.data()).doesNotContain("private"); }).verifyComplete();
    }
    @Test void cancellationReachesProviderPublisher() {
        var cancelled=new AtomicBoolean(); var model=model("unused");
        when(model.stream(any(Prompt.class))).thenReturn(Flux.<ChatResponse>never().doOnCancel(() -> cancelled.set(true)));
        StepVerifier.create(new LessonController(ChatClient.builder(model)).stream(new LessonController.Question("Hi")))
            .thenCancel().verify();
        assertThat(cancelled).isTrue();
    }
}
