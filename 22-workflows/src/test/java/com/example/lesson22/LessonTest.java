package com.example.lesson22;

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

    @Test void reviewRouteNeverRunsASecondModelCallOrWrite() {
        var model=model("{\"route\":\"REVIEW\"}");
        var result=new LessonController(ChatClient.builder(model)).route(new LessonController.Question("Change my account"));
        assertThat(result.requiresHumanApproval()).isTrue(); verify(model,times(1)).call(any(Prompt.class));
    }
    @Test void supportWorkflowHasExactlyTwoSteps() {
        var model=model("unused"); when(model.call(any(Prompt.class))).thenReturn(reply("{\"route\":\"SUPPORT\"}"),reply("Please visit the help desk."));
        assertThat(new LessonController(ChatClient.builder(model)).route(new LessonController.Question("I need help")).answer()).contains("help desk");
        verify(model,times(2)).call(any(Prompt.class));
    }
    @Test void invalidRouteFailsClosed() {
        var c=new LessonController(ChatClient.builder(model("{\"route\":\"EXECUTE_PAYMENT\"}")));
        assertThatThrownBy(() -> c.route(new LessonController.Question("Pay fees"))).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
}
