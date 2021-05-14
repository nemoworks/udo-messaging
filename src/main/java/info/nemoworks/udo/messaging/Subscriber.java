package info.nemoworks.udo.messaging;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Subscriber {

    public static final String TOPIC_UDO = "udo/+";

    private IMqttClient client;

    public Subscriber(IMqttClient client) {
        this.client = client;
    }

    public void subscribe() throws MqttException {
        if (!client.isConnected()) {
            client.connect();
        }

        client.subscribe(TOPIC_UDO, (topic, msg) -> {
            byte[] payload = msg.getPayload();
            log.info("Message received: topic={}, payload={}", topic, new String(payload));
        });
    }

}
