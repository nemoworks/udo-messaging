package info.nemoworks.udo.messaging;

import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.model.Udo;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HTTPServiceGatewayTest {

    private HTTPServiceGateway httpServiceGateway;
    //  private MessagingManager messagingManager;

    @BeforeEach
    public void setup() throws IOException {

        // messagingManager = new MessagingManager();

        httpServiceGateway = new HTTPServiceGateway();
    }

    @Test
    public void testHttpRequestAsync() throws InterruptedException {

        Udo udo1 = new Udo(null, null);
        ;
        udo1.setId("springboot-1");
//        udo1.uri = "http://localhost:8081/actuator/metrics/process.cpu.usage";

        Udo udo2 = new Udo(null, null);
        udo2.setId("r9s_g3kB2maipzb7EIRb");
//        udo2.uri = "http://localhost:8081/";

        // httpServiceGateway.register(udo1.getId(), URI.create(udo1.uri));
//        httpServiceGateway.register(udo2.getId(), URI.create(udo2.uri));

        httpServiceGateway.start();

    }

    @Test
    public void testControlHass() throws IOException, InterruptedException {
        httpServiceGateway
            .postRequestBody("http://192.168.43.35:8123/api/services/fan/turn_off".getBytes(),
                "{\"entity_id\": \"fan.zhimi_airpurifier_m1\"}");
    }
}
