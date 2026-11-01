package com.example.lesson13;

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

class LessonTest {
    @Test void splitsAndPreservesSourceMetadata() {
        var chunks=HandbookIndexer.chunks(); assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks).allSatisfy(d -> { assertThat(d.getText()).isNotBlank(); assertThat(d.getMetadata()).containsEntry("source","handbook").containsKey("chunk"); });
    }
    @Test void replacingSourceRemovesStaleChunksAndUsesStableIds() {
        var store=mock(VectorStore.class); var c=new LessonController(store); c.index();
        var order=inOrder(store); order.verify(store).delete("source == 'handbook'"); order.verify(store).add(any());
        assertThat(HandbookIndexer.chunks().stream().map(Document::getId).toList())
            .isEqualTo(HandbookIndexer.chunks().stream().map(Document::getId).toList());
    }
}
