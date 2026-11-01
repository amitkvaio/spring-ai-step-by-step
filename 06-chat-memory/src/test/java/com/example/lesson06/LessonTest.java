package com.example.lesson06;

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

    @Test void replaysSelectedMessagesAndIsolatesSessions() {
        var model=model("Hello"); var c=new LessonController(ChatClient.builder(model));
        c.ask("alice","My name is Asha"); c.ask("alice","What is my name?"); c.ask("bob","Hi");
        var prompts=ArgumentCaptor.forClass(Prompt.class); verify(model,times(3)).call(prompts.capture());
        assertThat(prompts.getAllValues().get(1).getContents()).contains("Asha");
        assertThat(prompts.getAllValues().get(2).getContents()).doesNotContain("Asha");
    }
    @Test void boundsAndClearsHistory() {
        var c=new LessonController(ChatClient.builder(model("Reply")));
        for(int i=0;i<8;i++) c.ask("a","Question "+i);
        assertThat(c.memory.get("a")).hasSizeLessThanOrEqualTo(6);
        c.memory.clear("a"); assertThat(c.memory.get("a")).isEmpty();
    }
}
