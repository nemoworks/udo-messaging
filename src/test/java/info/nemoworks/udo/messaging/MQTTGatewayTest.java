package info.nemoworks.udo.messaging;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.gateway.MQTTGateway;
import info.nemoworks.udo.messaging.messaging.Subscriber;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.GatewayEvent;
import java.io.IOException;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MQTTGatewayTest {

    MqttClient client1, client2, client3, client4;

    MQTTGateway mqttGateway;

    EventBus eventBus;

    @BeforeEach
    public void setup() throws MqttException, IOException {
        String clientid1 = UUID.randomUUID().toString();
        client1 = new MqttClient("tcp://210.28.132.168:30609", clientid1);

        String clientid2 = UUID.randomUUID().toString();
        client2 = new MqttClient("tcp://210.28.132.168:30609", clientid2);

        String clientid3 = UUID.randomUUID().toString();
        client3 = new MqttClient("tcp://210.28.132.168:30609", clientid3);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setUserName("udo-user");
        char[] password = "123456".toCharArray();
        options.setPassword(password);
        client1.connect(options);
        client2.connect(options);
        mqttGateway = new MQTTGateway();

        eventBus = new EventBus();
        eventBus.register(mqttGateway);
    }

    @Test
    public void testUpdateLink() throws MqttException, InterruptedException, IOException {
        Udo udo = new Udo();
        JsonObject jsonObject = new Gson().fromJson("{test: mqtt}", JsonObject.class);
        udo.setId("udo1");
        udo.setData(jsonObject);
        GatewayEvent gatewayEvent = new GatewayEvent(EventType.UPDATE, udo, null);

        // service
        Subscriber subscriber = new Subscriber(client3);
        subscriber.subscribe(udo.getId(), (topic, payload) -> {
            System.out.println("subscriber=====" + new String(payload.getPayload()));
        });

        // udo
        mqttGateway.downLink(udo.getId(), null);

        int i = 0;
        while (i < 10) {
            mqttGateway.subscribeMessage(gatewayEvent);
            Thread.sleep(1000);
            i++;
        }
    }

    @Test
    public void testDownLink() {

    }

    @Test
    public void testConnection() throws MqttException {
        String clientid4 = UUID.randomUUID().toString();
        client4 = new MqttClient("tcp://210.28.132.168:30609", clientid4);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
        options.setCleanSession(true);

        options.setConnectionTimeout(10);
        options.setUserName("udo-user");
        char[] password = "123456".toCharArray();
        options.setPassword(password);
        client4.connect(options);
        Subscriber subscriber = new Subscriber(client4);
        subscriber.subscribe("topic/pub", (topic, payload) -> {
            System.out.println("subscriber=====" + new String(payload.getPayload()));
        });
    }

}
