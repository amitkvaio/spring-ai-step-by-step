package com.example.lesson11;

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

import org.springframework.ai.embedding.EmbeddingModel;
class LessonTest {
    @Test void ranksMeaningVectorsInsteadOfExactWords() {
        var e=mock(EmbeddingModel.class);
        when(e.embed(any(String.class))).thenAnswer(call -> call.getArgument(0,String.class).contains("borrow") ? new float[]{0,1} : new float[]{1,0});
        var results=new LessonController(e).search(new LessonController.Query("Closing time?"));
        assertThat(results.getFirst().question()).contains("library");
        assertThat(results.getFirst().score()).isCloseTo(1.0,within(0.0001));
    }
    @Test void detectsDimensionMismatchAndZeroVector() {
        assertThatThrownBy(() -> LessonController.cosine(new float[]{1},new float[]{1,0})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LessonController.cosine(new float[]{0},new float[]{1})).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void perpendicularVectorsHaveZeroSimilarity() {
        assertThat(LessonController.cosine(new float[]{1,0},new float[]{0,1})).isZero();
    }
}
