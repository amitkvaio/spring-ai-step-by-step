package com.example.lesson17;

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

import java.math.BigDecimal;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.ai.chat.metadata.*;
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

    @Test void recordsUsageAndLatencyWithoutPromptTags() {
        var model=model("Hi");
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("Hi"))),
            ChatResponseMetadata.builder().usage(new DefaultUsage(100,50)).build()));
        var registry=new SimpleMeterRegistry();
        var reply=new LessonController(ChatClient.builder(model),registry,new BigDecimal("2"),new BigDecimal("4"))
            .chat(new LessonController.Question("Hello"));
        assertThat(reply.estimatedUsd()).isEqualByComparingTo("0.0004");
        assertThat(registry.get("course.chat.latency").tag("outcome","success").timer().count()).isEqualTo(1);
        assertThat(registry.get("course.tokens").tag("direction","input").counter().count()).isEqualTo(100);
    }
    @Test void missingUsageStaysUnknownAndFailuresAreTimed() {
        var model=model("Hi"); var registry=new SimpleMeterRegistry();
        var c=new LessonController(ChatClient.builder(model),registry,BigDecimal.ZERO,BigDecimal.ZERO);
        assertThat(c.chat(new LessonController.Question("Hi")).estimatedUsd()).isNull();
        when(model.call(any(Prompt.class))).thenThrow(new IllegalStateException("unavailable"));
        assertThatThrownBy(() -> c.chat(new LessonController.Question("Hi"))).isInstanceOf(IllegalStateException.class);
        assertThat(registry.get("course.chat.latency").tag("outcome","error").timer().count()).isEqualTo(1);
    }
}
