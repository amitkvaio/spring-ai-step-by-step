package com.example.lesson09;
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

    @Test void unauthenticatedUsersCannotCallAssistant() throws Exception {
        assertThat(request("/api/chat",null,"{\"question\":\"Stock?\"}").statusCode()).isEqualTo(401);
    }
    @Test void studentCannotApproveAnOrder() throws Exception {
        assertThat(request("/api/admin/orders","alice","{\"requestId\":\"a1\",\"quantity\":2,\"confirm\":true}").statusCode()).isEqualTo(403);
    }
    @Test void staffCanApproveAndDuplicateRequestDoesNotReduceStockAgain() throws Exception {
        String body="{\"requestId\":\"http-1\",\"quantity\":2,\"confirm\":true}";
        var first=request("/api/admin/orders","staff",body); var repeated=request("/api/admin/orders","staff",body);
        assertThat(first.statusCode()).isEqualTo(200); assertThat(repeated.body()).isEqualTo(first.body());
    }
}
