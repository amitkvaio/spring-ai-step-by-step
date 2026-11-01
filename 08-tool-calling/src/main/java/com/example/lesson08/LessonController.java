package com.example.lesson08;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import org.springframework.ai.tool.annotation.Tool;

@RestController
class LessonController {
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) {
        client=builder.defaultAdvisors(new ToolBudgetAdvisor()).build();
    }
    @PostMapping("/api/chat") Map<String,String> chat(@Valid @RequestBody Question q) {
        return Map.of("answer",client.prompt().user(q.question()).advisors(a -> a.param("modelCallCount",new java.util.concurrent.atomic.AtomicInteger())).tools(new ProductTools()).call().content());
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
}
class ProductTools {
    @Tool(description="Look up the current demo shop price in INR for PEN or NOTEBOOK. Never guess prices.")
    public BigDecimal price(String sku) {
        return switch(sku.toUpperCase(Locale.ROOT)) {
            case "PEN" -> new BigDecimal("10.00");
            case "NOTEBOOK" -> new BigDecimal("40.00");
            default -> throw new IllegalArgumentException("Unknown product");
        };
    }
}

class ToolBudgetAdvisor implements org.springframework.ai.chat.client.advisor.api.CallAdvisor {
    public String getName() { return "bounded-model-calls"; }
    public int getOrder() { return org.springframework.core.Ordered.LOWEST_PRECEDENCE-100; }
    public org.springframework.ai.chat.client.ChatClientResponse adviseCall(
            org.springframework.ai.chat.client.ChatClientRequest request,
            org.springframework.ai.chat.client.advisor.api.CallAdvisorChain chain) {
        var count=(java.util.concurrent.atomic.AtomicInteger)request.context().get("modelCallCount");
        if(count==null) {
            count=new java.util.concurrent.atomic.AtomicInteger();
            request=request.mutate().context("modelCallCount",count).build();
        }
        if(count.incrementAndGet()>4) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Tool loop exceeded four model calls.");
        return chain.nextCall(request);
    }
}
