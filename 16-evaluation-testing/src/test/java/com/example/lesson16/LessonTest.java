package com.example.lesson16;

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
import org.springframework.ai.evaluation.EvaluationRequest;
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

    @Test void computesRetrievalPrecisionAndRecallSeparately() {
        var report=new LessonController(ChatClient.builder(model("unused")),false)
            .evaluate(new LessonController.Case(List.of("library","borrowing"),List.of("library","fees")));
        assertThat(report.precision()).isEqualTo(0.5); assertThat(report.recall()).isEqualTo(0.5);
    }
    @Test void rejectsFabricatedCitationAndAcceptsKnownOne() {
        var evaluator=new CitationEvaluator(); var docs=List.of(new Document("Hours",Map.of("source","library")));
        assertThat(evaluator.evaluate(new EvaluationRequest("Hours?",docs,"Five [invented]")).isPass()).isFalse();
        assertThat(evaluator.evaluate(new EvaluationRequest("Hours?",docs,"Five [library]")).isPass()).isTrue();
    }
    @Test void modelJudgeRequiresExplicitOptIn() {
        var model=model("unused"); var c=new LessonController(ChatClient.builder(model),false);
        assertThatThrownBy(() -> c.judge(new LessonController.JudgeRequest("Q","Context","A"))).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verify(model,never()).call(any(Prompt.class));
    }
}
