package com.example.lesson10;

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

import org.mockito.ArgumentCaptor;
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

    static final String PNG="iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/lXcAAAAASUVORK5CYII=";
    @Test void sendsImageAndQuestion() {
        var model=model("A test image"); var c=new LessonController(ChatClient.builder(model));
        c.describe(new LessonController.Cover("Describe this cover",PNG));
        var prompt=ArgumentCaptor.forClass(Prompt.class); verify(model).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getMedia()).hasSize(1);
        assertThat(prompt.getValue().getContents()).contains("Describe this cover");
    }
    @Test void rejectsNonPngBeforeCallingModel() {
        var model=model("No"); var c=new LessonController(ChatClient.builder(model));
        assertThatThrownBy(() -> c.describe(new LessonController.Cover("Describe","aGVsbG8=")))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verify(model,never()).call(any(Prompt.class));
    }
}
