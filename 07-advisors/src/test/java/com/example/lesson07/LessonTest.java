package com.example.lesson07;

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

    @Test void blocksBeforeModelCall() {
        var model=model("Hi"); var c=new LessonController(ChatClient.builder(model));
        assertThatThrownBy(() -> c.chat(new LessonController.Question("Account 123456789012")))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verify(model,never()).call(any(Prompt.class)); assertThat(c.policy.completed).hasValue(0);
    }
    @Test void runsAfterSuccessfulModelCall() {
        var c=new LessonController(ChatClient.builder(model("Welcome")));
        assertThat(c.chat(new LessonController.Question("Help me"))).containsEntry("answer","Welcome");
        assertThat(c.policy.completed).hasValue(1);
    }
}
