package com.example.lesson23;

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

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
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

    @Test void reportsOllamaReachabilityWithoutGeneratingText() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/tags",exchange -> { exchange.sendResponseHeaders(200,-1); exchange.close(); }); server.start();
        try { assertThat(new OllamaHealth("http://127.0.0.1:"+server.getAddress().getPort()).health().getStatus().getCode()).isEqualTo("UP"); }
        finally { server.stop(0); }
    }
    @Test void missingProviderMakesReadinessDown() { assertThat(new OllamaHealth("http://127.0.0.1:1").health().getStatus().getCode()).isEqualTo("DOWN"); }
}
