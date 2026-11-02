package com.example.lesson01;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatQuestion {

    @NotBlank
    @Size(max = 1000)
    private String question;

    public ChatQuestion() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
