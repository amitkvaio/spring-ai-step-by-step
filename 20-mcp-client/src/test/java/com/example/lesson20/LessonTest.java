package com.example.lesson20;

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

import org.springframework.ai.tool.*;
import org.springframework.ai.tool.definition.ToolDefinition;
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

    @Test void listsTheToolsDiscoveredByTheProvider() {
        var tool=mock(ToolCallback.class); when(tool.getToolDefinition()).thenReturn(ToolDefinition.builder().name("shop_price").description("Read price").inputSchema("{}").build());
        ToolCallbackProvider provider=() -> new ToolCallback[]{tool};
        assertThat(new LessonController(ChatClient.builder(model("unused")),provider).tools()).containsExactly("shop_price");
    }
    @Test void ordinaryAnswerWorksWithDiscoveredTools() {
        ToolCallbackProvider provider=() -> new ToolCallback[0];
        var c=new LessonController(ChatClient.builder(model("A pen costs INR 10.")),provider);
        assertThat(c.chat(new LessonController.Question("Price of PEN?"))).containsEntry("answer","A pen costs INR 10.");
    }
}
