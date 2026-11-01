package com.example.lesson24;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.assertj.core.api.Assertions.assertThat;

// Real HTTP filters and validation. Rejected requests never need a model or database.
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties="spring.profiles.active=memory")
class AuthorizationHttpTest {
    @LocalServerPort int port;
    HttpResponse<String> request(String path,String user,String body) throws Exception {
        var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path));
        if(user!=null) request.header("Authorization","Basic "+Base64.getEncoder().encodeToString((user+":local-"+user).getBytes(StandardCharsets.UTF_8)));
        if(body!=null) request.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        try(var client=HttpClient.newHttpClient()) { return client.send(request.build(),HttpResponse.BodyHandlers.ofString()); }
    }

    @Test void askRequiresAuthentication() throws Exception {
        assertThat(request("/api/ask",null,"{\"question\":\"Hours?\",\"conversation\":\"c1\"}").statusCode()).isEqualTo(401);
    }
    @Test void onlyStaffCanReachDocumentIngestionAndOwnerIsValidated() throws Exception {
        String invalid="{\"source\":\"guide\",\"owner\":\"everyone\",\"text\":\"Some text\"}";
        assertThat(request("/api/admin/documents","alice",invalid).statusCode()).isEqualTo(403);
        assertThat(request("/api/admin/documents","staff",invalid).statusCode()).isEqualTo(400);
    }
    @Test void rejectsConversationScopeInjectionBeforeRetrieval() throws Exception {
        String invalid="{\"question\":\"Hours?\",\"conversation\":\"other:private\"}";
        assertThat(request("/api/ask","alice",invalid).statusCode()).isEqualTo(400);
    }
}
