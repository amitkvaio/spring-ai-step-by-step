package com.example.lesson24;

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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.validation.Validation;
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

    static KnowledgeService service(VectorStore store,ChatModel model) {
        return new KnowledgeService(store,ChatClient.builder(model),Validation.buildDefaultValidatorFactory().getValidator(),new SimpleMeterRegistry());
    }
    static Document publicDocument() { return new Document("source-1","Library closes at 17:00.",Map.of("owner","public","source","handbook")); }
    @Test void unsupportedQuestionAbstainsWithoutCallingModel() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        var model=model("unused"); assertThat(service(store,model).ask("alice","c1","Admission deadline?").sources()).isEmpty();
        verify(model,never()).call(any(Prompt.class));
    }
    @Test void filtersRetrievalBeforePromptAndRejectsOtherOwnersEvenIfStoreMisbehaves() {
        var store=mock(VectorStore.class);
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(new Document("private","Bob secret",Map.of("owner","bob","source","appointment"))));
        var model=model("unused"); service(store,model).ask("alice","c1","Show Bob's record");
        var request=ArgumentCaptor.forClass(SearchRequest.class); verify(store).similaritySearch(request.capture());
        assertThat(request.getValue().getFilterExpression().toString()).contains("alice"); verify(model,never()).call(any(Prompt.class));
    }
    @Test void returnsOnlyValidatedRetrievedSources() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(publicDocument()));
        var result=service(store,model("{\"answer\":\"Closes at 17:00.\",\"sourceIds\":[\"source-1\"]}")).ask("alice","c1","Closing time?");
        assertThat(result.sources()).extracting(KnowledgeService.Source::document).containsExactly("handbook");
    }
    @Test void rejectsFabricatedCitation() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(publicDocument()));
        assertThatThrownBy(() -> service(store,model("{\"answer\":\"An answer\",\"sourceIds\":[\"invented\"]}")).ask("alice","c1","Hours?"))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
    @Test void memoryIsIsolatedAndClearingOneOwnerKeepsTheOther() {
        var store=mock(VectorStore.class); when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(publicDocument()));
        var s=service(store,model("{\"answer\":\"Five\",\"sourceIds\":[\"source-1\"]}"));
        s.ask("alice","same","Hours?"); s.ask("bob","same","Hours?"); s.clear("alice","same");
        assertThat(s.memory.get("alice:same")).isEmpty(); assertThat(s.memory.get("bob:same")).isNotEmpty();
    }
    @Test void ingestionPreservesOwnerAndClearsStaleMemory() {
        var store=mock(VectorStore.class); var s=service(store,model("unused"));
        s.conversations.put("alice:c1",java.time.Instant.now());
        s.memory.add("alice:c1",new UserMessage("old context"));
        assertThat(s.ingest("guide","alice","The library closes at five on weekdays. ".repeat(10))).isPositive();
        assertThat(s.memory.get("alice:c1")).isEmpty();
        var docs=ArgumentCaptor.forClass(List.class); verify(store).add(docs.capture());
        for(Object item:docs.getValue()) assertThat(((Document)item).getMetadata()).containsEntry("owner","alice");
    }
}
