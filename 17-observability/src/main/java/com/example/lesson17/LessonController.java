package com.example.lesson17;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import io.micrometer.core.instrument.*;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.beans.factory.annotation.Value;

@RestController
class LessonController {
    private final ChatClient client; private final MeterRegistry registry;
    private final BigDecimal inputRate,outputRate;
    LessonController(ChatClient.Builder builder,MeterRegistry registry,
            @Value("${course.input-usd-per-million:0}") BigDecimal inputRate,@Value("${course.output-usd-per-million:0}") BigDecimal outputRate) {
        client=builder.build(); this.registry=registry; this.inputRate=inputRate; this.outputRate=outputRate;
    }
    @PostMapping("/api/chat") MeasuredReply chat(@Valid @RequestBody Question q) {
        var sample=io.micrometer.core.instrument.Timer.start(registry); String outcome="error";
        try {
            var response=client.prompt().user(q.question()).call().chatResponse();
            var usage=response.getMetadata().getUsage();
            boolean reported=usage!=null && usage.getPromptTokens()!=null && usage.getCompletionTokens()!=null
                && usage.getPromptTokens()+usage.getCompletionTokens()>0;
            if(reported) registry.counter("course.tokens","direction","input").increment(usage.getPromptTokens());
            if(reported) registry.counter("course.tokens","direction","output").increment(usage.getCompletionTokens());
            outcome="success";
            return new MeasuredReply(response.getResult().getOutput().getText(),reported?usage.getPromptTokens():null,
                reported?usage.getCompletionTokens():null,reported?estimate(usage,inputRate,outputRate):null);
        } finally { sample.stop(registry.timer("course.chat.latency","outcome",outcome)); }
    }
    static BigDecimal estimate(Usage usage,BigDecimal input,BigDecimal output) {
        return input.multiply(BigDecimal.valueOf(usage.getPromptTokens())).add(output.multiply(BigDecimal.valueOf(usage.getCompletionTokens())))
            .divide(new BigDecimal("1000000"));
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
    record MeasuredReply(String answer,Integer inputTokens,Integer outputTokens,BigDecimal estimatedUsd) {}
}
