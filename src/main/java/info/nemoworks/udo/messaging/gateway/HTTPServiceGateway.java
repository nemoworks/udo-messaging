package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.Subscribe;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UriType;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.GatewayEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class HTTPServiceGateway extends UdoGateway {

    private final HttpClient client;
    private final Builder httpRequestBuilder =
        HttpRequest.newBuilder()
            .header("Content-Type", "application/json")
            .timeout(Duration.ofMinutes(2));


    public ConcurrentHashMap<String, URI> getEndpoints() {
        return endpoints;
    }

    private final ConcurrentHashMap<String, URI> endpoints;

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
                .GET()
                .uri(URI.create(new String(payload)))
                .header("Referer", "postman")
                .build();

        return client.send(request, BodyHandlers.ofString());
    }


    @Subscribe
    public void gateWayEvent(GatewayEvent gatewayEvent) {
        try {
            Udo udo = (Udo) gatewayEvent.getSource();
            if (udo.getUri().getUriType().equals(UriType.MQTT)) {
                return;
            }
            EventType contextId = gatewayEvent.getContextId();
            switch (contextId) {
                case SAVE_BY_URI:
                    HttpResponse<String> uriBody = getRequestBody(gatewayEvent.getPayload());
                    this.updateUdoByUri(udo.getId(), uriBody.body().getBytes(),
                        gatewayEvent.getPayload(), udo.getContextInfo(), udo.getUri().getUriType());
                    break;
                case SAVE:
                    this.register(udo.getId(), new URI(udo.uri.getUri()));
                    break;
                case UPDATE:
                    if (gatewayEvent.getPayload() != null) {
                        HttpResponse<String> body = getRequestBody(gatewayEvent.getPayload());
                        this.updateUdoByPolling(udo.getId(), body.body().getBytes());
                    }
                    break;
                case DELETE:
                    this.unregister(udo.getId(), new URI(udo.uri.getUri()));
                    break;
                default:
                    break;
            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void register(String tag, URI uri) {
        if (!endpoints.containsKey(tag)) {
            endpoints.put(tag, uri);
        }
    }

    public synchronized void unregister(String tag, URI uri) {
        if (endpoints.containsKey(tag)) {
            endpoints.remove(tag, uri);
        }
    }

    public void start() throws InterruptedException {

//        for(int i = 0;i<5;i++){
//            endpoints.forEach(
//                    (key, value) -> {
//                        try {
//                            Thread.sleep(30000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            System.out.println("fetching data...");
//                            downLink(key, value.toString().getBytes());
//                        } catch (IOException | InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//
//                    });
//        }

//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//
//        executor.scheduleWithFixedDelay(
//            () -> {
//                try {

        endpoints.forEach(
            (key, value) -> {
                try {
                    System.out.println("fetching data...");
                    downLink(key, value.toString().getBytes());
                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

//                } catch (Exception ex) {
//                    throw new RuntimeException(ex);
//                }
//            },
//            15L,
//            15L,
//            TimeUnit.SECONDS);
//        TimeUnit.SECONDS.sleep(15L);

//        executor.shutdown();
    }

    // downlink: 获取资源状态，向udo发送状态更新消息
    @Override
    public void downLink(String tag, byte[] payload) throws IOException, InterruptedException {
        HttpRequest request =
            httpRequestBuilder
                .GET()
                .uri(URI.create(new String(payload)))
                .build();

        HttpResponse<String> body = client.send(request, BodyHandlers.ofString());

        this.updateUdoByPolling(tag, body.body().getBytes());
    }

    // updatelink: 参数为topic中监听到的更新请求 向资源发送状态更新请求
    @Override
    public void updateLink(String tag, byte[] payload, String data)
        throws IOException, InterruptedException {
        HttpRequest request = httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(data))
            .uri(URI.create(new String(payload)))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//        System.out.println(response.statusCode());
        // print response body
//        System.out.println(response.body());

    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
