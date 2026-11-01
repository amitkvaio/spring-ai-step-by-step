package com.example.lesson22;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
class LessonController {
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) { client=builder.build(); }
    @PostMapping("/api/workflow") Result route(@Valid @RequestBody Question q) {
        Decision decision;
        try {
            decision=client.prompt().system("Route a college request: LIBRARY for library hours, SUPPORT for general help, REVIEW for any write, order, payment or account change.")
                .user(q.question()).call().entity(Decision.class);
        } catch(RuntimeException e) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Could not classify the request."); }
        if(decision==null || decision.route()==null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Missing workflow route.");
        // The model proposes a route. Java chooses the permitted steps. No route performs writes.
        return switch(decision.route()) {
            case LIBRARY -> new Result(Route.LIBRARY,"The demo library opens 09:00–17:00 on weekdays.",false);
            case REVIEW -> new Result(Route.REVIEW,"A staff member must review this request. No action was performed.",true);
            case SUPPORT -> new Result(Route.SUPPORT,client.prompt().system("Draft a short help desk reply. Do not claim to perform account changes.").user(q.question()).call().content(),false);
        };
    }
    enum Route { LIBRARY,SUPPORT,REVIEW }
    record Decision(Route route) {}
    record Question(@NotBlank @Size(max=1000) String question) {}
    record Result(Route route,String answer,boolean requiresHumanApproval) {}
}
