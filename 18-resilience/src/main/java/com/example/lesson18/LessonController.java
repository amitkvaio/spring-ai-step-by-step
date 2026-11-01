package com.example.lesson18;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import org.springframework.web.reactive.function.client.*;

@RestController
class LessonController {
    private final ChatClient client; final RateGate gate; final Semaphore slots=new Semaphore(2);
    @org.springframework.beans.factory.annotation.Autowired
    LessonController(ChatClient.Builder builder) { this(builder,new RateGate(Clock.systemUTC())); }
    LessonController(ChatClient.Builder builder,RateGate gate) { client=builder.build(); this.gate=gate; }
    @PostMapping("/api/chat") Mono<Map<String,String>> chat(@Valid @RequestBody Question q) {
        return Mono.defer(() -> {
            if(!gate.allow() || !slots.tryAcquire()) return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Local request limit reached."));
            // No tools or writes are attached, so retrying generation has no application side effect.
            return client.prompt().user(q.question()).stream().content().collectList()
                .map(parts -> Map.of("answer",String.join("",parts)))
                .retryWhen(Retry.backoff(1,Duration.ofMillis(100)).filter(LessonController::transientFailure)
                    .onRetryExhaustedThrow((spec,signal) -> signal.failure()))
                .timeout(Duration.ofSeconds(5)) // Overall deadline includes the retry and its backoff.
                .onErrorMap(error -> new ResponseStatusException(error instanceof TimeoutException?HttpStatus.GATEWAY_TIMEOUT:HttpStatus.BAD_GATEWAY,"The model request failed within its bounded policy."))
                .doFinally(signal -> slots.release());
        });
    }
    static boolean transientFailure(Throwable error) {
        return error instanceof WebClientRequestException || error instanceof org.springframework.ai.retry.TransientAiException
            || (error instanceof WebClientResponseException response && (response.getStatusCode().value()==429 || response.getStatusCode().is5xxServerError()));
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
}
class RateGate {
    private final Clock clock; private long minute=-1; private int used;
    RateGate(Clock clock) { this.clock=clock; }
    synchronized boolean allow() {
        long now=clock.millis()/60000;
        if(now!=minute) { minute=now; used=0; }
        return ++used<=10;
    }
}
