package com.example.lesson10;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeTypeUtils;

@RestController
class LessonController {
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) { client=builder.build(); }
    @PostMapping("/api/describe") Map<String,String> describe(@Valid @RequestBody Cover cover) {
        byte[] image=png(cover.imageBase64());
        return Map.of("answer",client.prompt().user(u -> u.text(cover.question())
            .media(MimeTypeUtils.IMAGE_PNG,new ByteArrayResource(image))).call().content());
    }
    static byte[] png(String base64) {
        try {
            byte[] bytes=Base64.getDecoder().decode(base64);
            byte[] signature={(byte)137,80,78,71,13,10,26,10};
            if(bytes.length>1_000_000 || bytes.length<8 || !Arrays.equals(Arrays.copyOf(bytes,8),signature))
                throw new IllegalArgumentException();
            return bytes;
        } catch(IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supply a PNG image under 1 MB."); }
    }
    record Cover(@NotBlank @Size(max=1000) String question,@NotBlank @Size(max=1_333_336) String imageBase64) {}
}
