package com.example.lesson19;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;

@RestController
class LessonController {
    @GetMapping("/api/catalog") Map<String,Object> catalog() { return Map.of("PEN",10,"NOTEBOOK",40,"currency","INR"); }
    @Bean ToolCallbackProvider shopTools() { return MethodToolCallbackProvider.builder().toolObjects(new ShopTools()).build(); }
}
class ShopTools {
    @Tool(description="Read the demo shop's price in INR for PEN or NOTEBOOK. This tool is read-only.")
    public int price(String sku) {
        return switch(sku.toUpperCase(Locale.ROOT)) { case "PEN" -> 10; case "NOTEBOOK" -> 40; default -> throw new IllegalArgumentException("Unknown product"); };
    }
}
