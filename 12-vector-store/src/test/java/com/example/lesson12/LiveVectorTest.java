package com.example.lesson12;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.vectorstore.*;
import org.springframework.ai.document.Document;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
@EnabledIfEnvironmentVariable(named="LIVE_VECTOR_TESTS",matches="true")
@SpringBootTest
class LiveVectorTest {
    @Autowired VectorStore store;
    @Test void writesAndRetrievesRealPgvectorEmbedding() {
        String id=UUID.randomUUID().toString();
        try {
            store.add(List.of(new Document(id,"The integration-test library closes at five.",Map.of("kind","live-test"))));
            assertThat(store.similaritySearch(SearchRequest.builder().query("library closing time").filterExpression("kind == 'live-test'").build()))
                .extracting(Document::getId).contains(id);
        } finally { store.delete(List.of(id)); }
    }
}
