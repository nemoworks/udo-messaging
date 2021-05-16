package info.nemoworks.udo.messaging;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PubSubTest {

    IMqttClient client1, client2;

    @BeforeEach
    public void setup() throws MqttException {

        String clientid1 = UUID.randomUUID().toString();
        client1 = new MqttClient("tcp://test.mosquitto.org:1883", clientid1);

        String clientid2 = UUID.randomUUID().toString();
        client2 = new MqttClient("tcp://test.mosquitto.org:1883", clientid2);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        client1.connect(options);
        client2.connect(options);

    }

    @Test
    public void whensubandpub_thenSuccess() throws MqttException, InterruptedException {

        // CountDownLatch receivedSignal = new CountDownLatch(1);

        // Subscriber subscriber = new Subscriber(client1);
        // Udo udo = new Udo(null, null);

        // subscriber.subscribe(udo.);

        // Publisher publisher = new Publisher(client2);

        // udo.setId(UUID.randomUUID().toString());

        // publisher.publish(udo, "hello udo");

        // receivedSignal.await(10, TimeUnit.SECONDS);

        // client1.close();
        // client2.close();


    }

}
