package com.example.lesson15;

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

    @Test void searchesOriginalAndRewrittenQueryAndDeduplicates() {
        var store=mock(VectorStore.class);
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(LessonController.approvedDocuments());
        var c=new LessonController(store,ChatClient.builder(model("library closing time")));
        assertThat(c.retrieve(new LessonController.Query("When can I visit?",2,0.4,true))).hasSize(2);
        var searches=ArgumentCaptor.forClass(SearchRequest.class); verify(store,times(2)).similaritySearch(searches.capture());
        assertThat(searches.getAllValues()).extracting(SearchRequest::getQuery).containsExactly("When can I visit?","library closing time");
        assertThat(searches.getAllValues()).allSatisfy(q -> { assertThat(q.getSimilarityThreshold()).isEqualTo(0.4); assertThat(q.hasFilterExpression()).isTrue(); });
    }
    @Test void plainRetrievalDoesNotCallModel() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        var model=model("unused"); new LessonController(store,ChatClient.builder(model)).retrieve(new LessonController.Query("Library?",2,0.5,false));
        verify(model,never()).call(any(Prompt.class));
    }
}
