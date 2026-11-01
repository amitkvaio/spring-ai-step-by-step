package com.example.lesson21;
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

    @Test void documentEndpointRequiresAuthentication() throws Exception {
        assertThat(request("/api/documents",null,null).statusCode()).isEqualTo(401);
    }
    @Test void eachLoginSeesOnlyItsOwnPrivateDocuments() throws Exception {
        var alice=request("/api/documents","alice",null); var bob=request("/api/documents","bob",null);
        assertThat(alice.statusCode()).isEqualTo(200); assertThat(alice.body()).contains("Tuesday").doesNotContain("Friday");
        assertThat(bob.body()).contains("Friday").doesNotContain("Tuesday");
    }
    @Test void studentsCannotApproveDocumentsAndStaffInputsAreValidated() throws Exception {
        String invalid="{\"owner\":\"everyone\",\"text\":\"Not approved\"}";
        assertThat(request("/api/admin/documents","alice",invalid).statusCode()).isEqualTo(403);
        assertThat(request("/api/admin/documents","staff",invalid).statusCode()).isEqualTo(400);
    }
}
