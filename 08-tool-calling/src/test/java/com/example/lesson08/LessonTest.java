package com.example.lesson08;

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

    @Test void applicationExecutesRequestedToolThenModelReceivesResult() {
        var model=model("unused");
        var tool=AssistantMessage.builder().content("").toolCalls(List.of(
            new AssistantMessage.ToolCall("p1","function","price","{\"sku\":\"PEN\"}"))).build();
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(tool))),reply("A pen costs INR 10."));
        var c=new LessonController(ChatClient.builder(model));
        assertThat(c.chat(new LessonController.Question("Price of PEN?"))).containsEntry("answer","A pen costs INR 10.");
        var prompts=ArgumentCaptor.forClass(Prompt.class); verify(model,times(2)).call(prompts.capture());
        assertThat(prompts.getAllValues().get(1).getInstructions().stream().filter(ToolResponseMessage.class::isInstance).map(ToolResponseMessage.class::cast).findFirst().orElseThrow().getResponses().getFirst().responseData()).contains("10.00");
    }
    @Test void unknownSkuIsRejected() { assertThatThrownBy(() -> new ProductTools().price("OTHER")).isInstanceOf(IllegalArgumentException.class); }
    @Test void endlessToolRequestsAreBounded() {
        var model=model("unused");
        var tool=AssistantMessage.builder().content("").toolCalls(List.of(
            new AssistantMessage.ToolCall("p1","function","price","{\"sku\":\"PEN\"}"))).build();
        when(model.call(any(Prompt.class))).thenAnswer(call -> new ChatResponse(List.of(new Generation(AssistantMessage.builder().content("").toolCalls(List.of(new AssistantMessage.ToolCall(UUID.randomUUID().toString(),"function","price","{\"sku\":\"PEN\"}"))).build()))));
        assertThatThrownBy(() -> new LessonController(ChatClient.builder(model)).chat(new LessonController.Question("Price?")))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verify(model,times(4)).call(any(Prompt.class));
    }
}
