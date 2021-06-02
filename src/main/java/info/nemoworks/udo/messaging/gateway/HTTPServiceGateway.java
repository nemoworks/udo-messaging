package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.Subscribe;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.GatewayEvent;
import info.nemoworks.udo.model.event.UdoEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class HTTPServiceGateway extends UdoGateway {

    private final HttpClient client;
    private final Builder httpRequestBuilder =
        HttpRequest.newBuilder()
            .header("Content-Type", "application/json")
            .GET()
            .timeout(Duration.ofMinutes(2));

    public ConcurrentHashMap<String, URI> getEndpoints() {
        return endpoints;
    }

    private static volatile ConcurrentHashMap<String, URI> endpoints;

    public HTTPServiceGateway() {
        super();
        endpoints = new ConcurrentHashMap<>();
        client =
            HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    HttpResponse<String> getRequestBody(byte[] payload) throws IOException, InterruptedException {
        HttpRequest request =
            httpRequestBuilder
                .uri(URI.create(new String(payload)))
                .build();

        return client.send(request, BodyHandlers.ofString());
    }


    @Subscribe
    public void gateWayEvent(GatewayEvent gatewayEvent) {
        try {
            Udo udo = (Udo) gatewayEvent.getSource();
            EventType contextId = gatewayEvent.getContextId();
            switch (contextId) {
                case SAVE_BY_URI:
                    HttpResponse<String> uriBody = getRequestBody(gatewayEvent.getPayload());
                    this.updateUdoByUri(udo.getId(), uriBody.body().getBytes(),
                            gatewayEvent.getPayload());
                    break;
                case SAVE:
                    this.register(udo.getId(), new URI(udo.uri));
                    break;
                case UPDATE:
                    HttpResponse<String> body = getRequestBody(gatewayEvent.getPayload());
                    this.updateUdoByPolling(udo.getId(), body.body().getBytes());
                    break;
                case DELETE:
                    this.unregister(udo.getId(), new URI(udo.uri));
                    break;
                default:
                    break;
            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void register(String tag, URI uri) {
        if (endpoints.containsKey(tag)) {
            endpoints.put(tag, uri);
        }
    }

    public synchronized void unregister(String tag, URI uri) {
        if (endpoints.containsKey(tag)) {
            endpoints.remove(tag, uri);
        }
    }

    public void start() throws InterruptedException {

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(
            () -> {
                try {
                    System.out.println("fetching data...");
                    endpoints.forEach(
                        (key, value) -> {
                            try {
                                downlink(key, value.toString().getBytes());
                            } catch (IOException | InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        });

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            },
            1,
            1,
            TimeUnit.SECONDS);
        //        TimeUnit.SECONDS.sleep(15L);

        executor.shutdown();
    }

    @Override
    public void downlink(String tag, byte[] payload) throws IOException, InterruptedException {

        HttpRequest request =
            httpRequestBuilder
                .uri(URI.create(new String(payload)))
                .build();

        HttpResponse<String> body = client.send(request, BodyHandlers.ofString());

        this.updateUdoByPolling(tag, body.body().getBytes());
    }
}
