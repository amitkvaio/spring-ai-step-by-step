package com.example.lesson13;

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
    @PostMapping("/api/index") Map<String,Integer> index() {
        var chunks=HandbookIndexer.chunks();
        // Remove stale chunks only from this source before replacing its index.
        store.delete("source == 'handbook'"); store.add(chunks);
        return Map.of("chunks",chunks.size());
    }
    @GetMapping("/api/chunks") List<Document> chunks() { return HandbookIndexer.chunks(); }
}

class HandbookIndexer {
    static List<Document> chunks() {
        var reader=new org.springframework.ai.reader.TextReader(new org.springframework.core.io.ClassPathResource("college-handbook.txt"));
        reader.getCustomMetadata().put("source","handbook");
        var split=org.springframework.ai.transformer.splitter.TokenTextSplitter.builder().withChunkSize(40)
            .withMinChunkSizeChars(20).withMinChunkLengthToEmbed(5).build().apply(reader.get());
        var result=new ArrayList<Document>();
        for(int i=0;i<split.size();i++) {
            var text=split.get(i).getText();
            String id=UUID.nameUUIDFromBytes(("handbook:"+i).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
            result.add(new Document(id,text,Map.of("source","handbook","chunk",i)));
        }
        return result;
    }
}

@Configuration
class StoreConfig {
    @Bean VectorStore vectorStore(EmbeddingModel embeddings) { return SimpleVectorStore.builder(embeddings).build(); }
}
