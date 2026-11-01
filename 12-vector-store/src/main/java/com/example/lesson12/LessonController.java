package com.example.lesson12;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.*;
import org.springframework.context.annotation.*;

@RestController
class LessonController {
    private final VectorStore store;
    LessonController(VectorStore store) { this.store=store; }
    @PostMapping("/api/index") Map<String,Integer> index() { var docs=approvedDocuments(); store.add(docs); return Map.of("indexed",docs.size()); }
    @PostMapping("/api/search") List<Hit> search(@Valid @RequestBody Query q) {
        return store.similaritySearch(SearchRequest.builder().query(q.question()).topK(3)
            .filterExpression("kind == 'faq'").build()).stream().map(d -> new Hit(d.getId(),d.getText(),d.getScore())).toList();
    }
    record Query(@NotBlank @Size(max=1000) String question) {}
    record Hit(String id,String text,Double score) {}

    static List<Document> approvedDocuments() {
        return List.of(
            new Document("6db11ae8-1f5b-42ea-b754-32d09987eb31","The college library closes at 17:00 Monday to Friday.",Map.of("source","library","kind","faq")),
            new Document("9abf22ae-5c9a-4e80-a39c-e48b7da2b781","Students can borrow three books for fourteen days.",Map.of("source","borrowing","kind","faq")));
    }
}

@Configuration
class StoreConfig {
    @Bean @Profile("!memory") VectorStore persistent(EmbeddingModel embeddings,
            @org.springframework.beans.factory.annotation.Value("${course.jdbc-url}") String url,
            @org.springframework.beans.factory.annotation.Value("${course.db-user}") String user,
            @org.springframework.beans.factory.annotation.Value("${course.db-password}") String password) {
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(new org.springframework.jdbc.datasource.DriverManagerDataSource(url,user,password));
        return org.springframework.ai.vectorstore.pgvector.PgVectorStore.builder(jdbc,embeddings)
            .dimensions(768).initializeSchema(true).build();
    }
    @Bean @Profile("memory") VectorStore memory(EmbeddingModel embeddings) { return SimpleVectorStore.builder(embeddings).build(); }
}
