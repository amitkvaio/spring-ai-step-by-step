package com.example.lesson25;

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

    @Test void comparisonUsesTheConfiguredSecondModel() {
        var model=model("Reply"); var c=new LessonController(ChatClient.builder(model),"another-local-model");
        c.compare(new LessonController.Question("Recommend a library book"));
        var prompts=ArgumentCaptor.forClass(Prompt.class); verify(model,times(2)).call(prompts.capture());
        assertThat(prompts.getAllValues().get(1).getOptions().getModel()).isEqualTo("another-local-model");
    }
    @Test void nativeOutputMapsToRecordAndCapabilitiesAreExplicit() {
        var c=new LessonController(ChatClient.builder(model("{\"title\":\"A Book\",\"reason\":\"Easy to read\"}")),"llama3.2:3b");
        assertThat(c.nativeJson(new LessonController.Question("Recommend a book")).title()).isEqualTo("A Book");
        assertThat(c.capabilities().get("audioGeneration")).contains("not implemented");
    }
}
