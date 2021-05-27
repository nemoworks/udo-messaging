package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.Subscribe;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.UdoEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.*;

@Component
public class HTTPServiceGateway extends UdoGateway {

    private final HttpClient client;

    public ConcurrentHashMap<String, URI> getEndpoints() {
        return endpoints;
    }

    private volatile static ConcurrentHashMap<String, URI> endpoints;

    public HTTPServiceGateway() {
        super();
        endpoints = new ConcurrentHashMap<>();

        client = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20)).build();
    }

    HttpResponse<String> getRequestBody(byte[] payload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(new String(payload)))
                .header("Content-Type", "application/json").GET().timeout(Duration.ofMinutes(2))
                .build();
        return client.send(request, BodyHandlers.ofString());
    }

    @Subscribe
    public void storageEvent(UdoEvent storageEvent) {
        try {
            Udo udo = (Udo) storageEvent.getSource();
            EventType contextId = storageEvent.getContextId();
            switch (contextId) {
                case SAVE_BY_URI:
                    HttpResponse<String> uriBody = getRequestBody(storageEvent.getPayload());
                    this.updateUdoByUri(udo.getId(), uriBody.body().getBytes(), storageEvent.getPayload());
                    break;
                case SAVE:
                    this.register(udo.getId(), new URI(udo.uri));
                    break;
                default:
                    break;
            }
        } catch (InterruptedException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void gateWayEvent(UdoEvent gatewayEvent) {
        try {
            Udo udo = (Udo) gatewayEvent.getSource();
            EventType contextId = gatewayEvent.getContextId();
            switch (contextId) {
                case SAVE:
                    this.register(udo.getId(), new URI(udo.uri));
                    break;
                case SYNC:
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
        if (endpoints.containsKey(tag))
            endpoints.put(tag, uri);
    }

    public synchronized void unregister(String tag, URI uri) {
        if (endpoints.containsKey(tag))
            endpoints.remove(tag, uri);
    }

    public void start() throws InterruptedException {

        CountDownLatch receivedSignal = new CountDownLatch(10);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {

                System.out.println("fetching data...");
                this.endpoints.entrySet().forEach(entry -> {
                    try {
                        downlink(entry.getKey(), entry.getValue().toString().getBytes());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                });
                receivedSignal.countDown();

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, 1, 1, TimeUnit.SECONDS);

        receivedSignal.await(1, TimeUnit.MINUTES);

        executor.shutdown();

    }

    @Override
    public void downlink(String tag, byte[] payload) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(new String(payload)))
                .header("Content-Type", "application/json").GET().timeout(Duration.ofMinutes(2))
                .build();
        HttpResponse<String> body = client.send(request, BodyHandlers.ofString());
        this.updateUdoByPolling(tag, body.body().getBytes());
    }


}