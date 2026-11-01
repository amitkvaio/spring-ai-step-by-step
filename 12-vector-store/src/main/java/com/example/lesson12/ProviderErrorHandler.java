package com.example.lesson12;

import org.springframework.ai.retry.TransientAiException;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
class ProviderErrorHandler {
    @ExceptionHandler({RestClientException.class, TransientAiException.class, NonTransientAiException.class})
    ProblemDetail providerUnavailable(RuntimeException exception) {
        // Do not send provider response bodies or internal connection details to callers.
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "The model service could not complete the request. Check Ollama and the model name.");
    }
}
