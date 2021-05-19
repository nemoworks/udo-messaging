package info.nemoworks.udo.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UdoType;
import info.nemoworks.udo.service.UdoServiceException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class HTTPServiceGateway extends UdoGateway {

        private final HttpClient client;

        public Map<String, URI> getEndpoints() {
                return endpoints;
        }

        private final Map<String, URI> endpoints;

        public HTTPServiceGateway(MessagingManager messagingManager) {
                super(messagingManager);

                endpoints = new HashMap<>();

                client = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
                                .connectTimeout(Duration.ofSeconds(20)).build();
        }

        public void register(String tag, URI uri) {
                endpoints.put(tag, uri);
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
                List<UdoType> types = udoService.getAllTypes();
                client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
                                .thenAccept(body -> this.updateUdo(tag, body.getBytes()));
        }


}
