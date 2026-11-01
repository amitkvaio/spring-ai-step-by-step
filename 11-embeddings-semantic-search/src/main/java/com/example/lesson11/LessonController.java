package com.example.lesson11;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.embedding.EmbeddingModel;

@RestController
class LessonController {
    private final EmbeddingModel embeddings;
    final List<String> questions=List.of("When does the library close?","How many books can I borrow?","Where can I get account help?");
    LessonController(EmbeddingModel embeddings) { this.embeddings=embeddings; }
    @PostMapping("/api/search") List<Match> search(@Valid @RequestBody Query q) {
        float[] query=embeddings.embed(q.question());
        var results=new ArrayList<Match>();
        for(String text:questions) results.add(new Match(text,cosine(query,embeddings.embed(text))));
        return results.stream().sorted(Comparator.comparingDouble(Match::score).reversed()).limit(2).toList();
    }
    static double cosine(float[] a,float[] b) {
        if(a.length==0 || a.length!=b.length) throw new IllegalArgumentException("Embedding dimensions differ.");
        double dot=0,aa=0,bb=0;
        for(int i=0;i<a.length;i++) { dot+=a[i]*b[i]; aa+=a[i]*a[i]; bb+=b[i]*b[i]; }
        if(aa==0 || bb==0) throw new IllegalArgumentException("A zero vector has no cosine direction.");
        return dot/Math.sqrt(aa*bb);
    }
    record Query(@NotBlank @Size(max=1000) String question) {}
    record Match(String question,double score) {}
}
