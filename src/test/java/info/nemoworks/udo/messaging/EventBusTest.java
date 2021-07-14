package info.nemoworks.udo.messaging;

import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.service.UdoService;
import info.nemoworks.udo.service.UdoServiceException;
import info.nemoworks.udo.storage.UdoNotExistException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EventBusTest {

    @Autowired
    UdoService udoService;

    private HTTPServiceGateway httpServiceGateway;

    @Autowired
    EventBus eventBus;

    @BeforeEach
    public void setup() throws IOException {
        httpServiceGateway = new HTTPServiceGateway();
        eventBus.register(httpServiceGateway);
    }

    @Test
    public void testSubscribe() throws UdoServiceException, UdoNotExistException {
        Udo udo = new Udo(null, null);
        udo.setId("r9s_g3kB2maipzb7EIRb");
//        udo.uri = "http://localhost:8081/";
        udoService.saveOrUpdateUdo(udo);
    }
}
