package com.example.lesson14;

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

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.*;
import org.springframework.context.annotation.*;

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

    @Test void noPassagesMeansNoModelCall() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        var model=model("unused"); var result=new LessonController(store,ChatClient.builder(model)).ask(new LessonController.Query("Admission deadline?"));
        assertThat(result.sources()).isEmpty(); verify(model,never()).call(any(Prompt.class));
    }
    @Test void placesRetrievedPassagesInPromptAndReturnsTheirSources() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(LessonController.approvedDocuments().getFirst()));
        var model=model("Closes at 17:00 [library]");
        var result=new LessonController(store,ChatClient.builder(model)).ask(new LessonController.Query("Closing time?"));
        var prompt=ArgumentCaptor.forClass(Prompt.class); verify(model).call(prompt.capture());
        assertThat(prompt.getValue().getContents()).contains("17:00"); assertThat(result.sources()).containsExactly("library");
    }
}
