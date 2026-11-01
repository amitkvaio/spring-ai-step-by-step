package com.example.lesson12;

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
    @Test void sendsVectorSearchAndMetadataFilterToStore() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        new LessonController(store).search(new LessonController.Query("Closing time?"));
        var query=ArgumentCaptor.forClass(SearchRequest.class); verify(store).similaritySearch(query.capture());
        assertThat(query.getValue().getTopK()).isEqualTo(3); assertThat(query.getValue().hasFilterExpression()).isTrue();
    }
    @Test void reindexUsesStableIds() {
        var first=LessonController.approvedDocuments(); var second=LessonController.approvedDocuments();
        assertThat(first.stream().map(Document::getId).toList()).isEqualTo(second.stream().map(Document::getId).toList());
        assertThat(first).allSatisfy(d -> assertThat(d.getMetadata()).containsEntry("kind","faq"));
    }
}
