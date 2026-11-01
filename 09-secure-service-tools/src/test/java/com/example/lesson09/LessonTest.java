package com.example.lesson09;

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

    @Test void readToolDoesNotChangeStockAndUsesServerIdentity() {
        var inventory=new InventoryService(); var tool=new StockTools(inventory,"alice");
        assertThat(tool.stock()).containsEntry("requestedBy","alice").containsEntry("available",10);
        assertThat(inventory.stock()).isEqualTo(10);
    }
    @Test void explicitOrderIsIdempotent() {
        var inventory=new InventoryService();
        assertThat(inventory.order("staff","req-1",2)).isEqualTo(inventory.order("staff","req-1",2));
        assertThat(inventory.stock()).isEqualTo(8);
    }
    @Test void missingConfirmationIsRejected() {
        var c=new LessonController(ChatClient.builder(model("Hi")));
        assertThatThrownBy(() -> c.approve(new LessonController.Order("r1",2,false),() -> "staff"))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThat(c.inventory.stock()).isEqualTo(10);
    }
}
