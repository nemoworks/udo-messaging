package info.nemoworks.udo.messaging.messaging;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Subscriber {

    private MqttClient client;

    public Subscriber(MqttClient client) {
        this.client = client;
    }

    public void subscribe(String mqttTopic, IMqttMessageListener listener) throws MqttException {
        if (!client.isConnected()) {
            client.connect();
            log.debug("client" + client.getClientId() + "connected");
        }
        client.subscribe(mqttTopic, listener);
    }

    public void unsubscribe(String mqttTopic) throws MqttException {
        if (!client.isConnected()) {
            client.connect();
            log.debug("client" + client.getClientId() + "connected");
        }
        client.unsubscribe(mqttTopic);
    }

}
