package info.nemoworks.udo.messaging;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPServiceGatewayTest {
    private HttpClient client;

    @BeforeEach
    public void setup(){
        this.client  = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20)).build();
    }

    @Test
    public void testHttpRequestAsync(){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://my-json-server.typicode.com/typicode/demo/posts/1"))
                .timeout(Duration.ofMinutes(2)).build();
        client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
                .thenAccept(log::info);
    }
}
