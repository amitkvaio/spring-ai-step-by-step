package com.example.lesson15;

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
    private final VectorStore store; private final ChatClient client;
    LessonController(VectorStore store,ChatClient.Builder builder) { this.store=store; client=builder.build(); }
    @PostMapping("/api/index") Map<String,Integer> index() { store.add(approvedDocuments()); return Map.of("indexed",2); }
    @PostMapping("/api/retrieve") List<Hit> retrieve(@Valid @RequestBody Query q) {
        var queries=new LinkedHashSet<String>(); queries.add(q.question());
        if(q.rewrite()) queries.add(client.prompt().system("Rewrite as a short library search query. Return only the query; do not answer.")
            .user(q.question()).call().content());
        var unique=new HashMap<String,Document>();
        for(String query:queries) {
            for(var doc:store.similaritySearch(SearchRequest.builder().query(query).topK(q.topK())
                .similarityThreshold(q.minScore()).filterExpression("kind == 'faq'").build()))
                unique.merge(doc.getId(),doc,(a,b) -> score(a)>=score(b)?a:b);
        }
        return unique.values().stream().sorted(Comparator.comparingDouble(LessonController::score).reversed())
            .limit(q.topK()).map(d -> new Hit(d.getId(),d.getText(),d.getMetadata().get("source").toString(),score(d))).toList();
    }
    static double score(Document d) { return d.getScore()==null?0:d.getScore(); }
    record Query(@NotBlank @Size(max=1000) String question,@Min(1) @Max(5) int topK,@DecimalMin("0") @DecimalMax("1") double minScore,boolean rewrite) {}
    record Hit(String id,String text,String source,double score) {}

    static List<Document> approvedDocuments() {
        return List.of(
            new Document("6db11ae8-1f5b-42ea-b754-32d09987eb31","The college library closes at 17:00 Monday to Friday.",Map.of("source","library","kind","faq")),
            new Document("9abf22ae-5c9a-4e80-a39c-e48b7da2b781","Students can borrow three books for fourteen days.",Map.of("source","borrowing","kind","faq")));
    }
}

@Configuration
class StoreConfig {
    @Bean VectorStore vectorStore(EmbeddingModel embeddings) { return SimpleVectorStore.builder(embeddings).build(); }
}
