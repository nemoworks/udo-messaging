package info.nemoworks.udo.messaging;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import info.nemoworks.udo.model.Udo;

public class HTTPServiceGateway extends UdoGateway {

    private HttpClient client;

    public HTTPServiceGateway(MessagingManager messagingManager) {
        super(messagingManager);

        client = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20)).build();
    }

    @Override
    public void downlink(String appId, Udo udo, byte[] payload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://my-json-server.typicode.com/typicode/demo/posts"))
                .timeout(Duration.ofMinutes(2)).build();
        client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
                .thenAccept(result -> this.uplink(appId, udo, result.getBytes()));
    }

}
