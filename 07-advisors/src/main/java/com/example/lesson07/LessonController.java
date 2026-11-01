package com.example.lesson07;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
class LessonController {
    final DeskPolicyAdvisor policy=new DeskPolicyAdvisor();
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) { client=builder.defaultAdvisors(policy).build(); }
    @PostMapping("/api/chat") Map<String,String> chat(@Valid @RequestBody Question q) {
        return Map.of("answer",client.prompt().user(q.question()).call().content());
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
}

class DeskPolicyAdvisor implements CallAdvisor {
    final AtomicInteger completed=new AtomicInteger();
    public String getName() { return "desk-policy"; }
    public int getOrder() { return 0; }
    public ChatClientResponse adviseCall(ChatClientRequest request,CallAdvisorChain chain) {
        // A deliberately narrow classroom rule, not a complete sensitive-data detector.
        if(request.prompt().getContents().matches("(?s).*\\b[0-9]{12}\\b.*"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Remove the 12-digit account number.");
        var response=chain.nextCall(request);
        completed.incrementAndGet(); // Runs after a successful model call.
        return response;
    }
}
