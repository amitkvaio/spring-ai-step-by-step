package com.example.lesson10;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@SpringBootApplication
@RestController
public class LessonApplication {
    public static void main(String[] args) { SpringApplication.run(LessonApplication.class,args); }
    @GetMapping("/") Map<String,Object> status() { return Map.of("lesson",10,"status","running"); }
}
