package com.example.lesson20;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.tool.*;

@RestController
class LessonController {
    private final ChatClient client; private final ToolCallbackProvider tools;
    LessonController(ChatClient.Builder builder,ToolCallbackProvider tools) {
        client=builder.defaultAdvisors(new ToolBudgetAdvisor()).build(); this.tools=tools;
    }
    @GetMapping("/api/tools") List<String> tools() {
        return Arrays.stream(tools.getToolCallbacks()).map(c -> c.getToolDefinition().name()).toList();
    }
    @PostMapping("/api/chat") Map<String,String> chat(@Valid @RequestBody Question q) {
        return Map.of("answer",client.prompt().user(q.question())
            .advisors(a -> a.param("modelCallCount",new java.util.concurrent.atomic.AtomicInteger()))
            .toolCallbacks(tools).call().content());
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
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
