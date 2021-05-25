package info.nemoworks.udo.messaging.messaging;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Component
public class Publisher {

    final MqttClient client;

    public Publisher(MqttClient client) {
        this.client = client;
    }


    public void publish(String topic, byte[] payload) throws MqttException {
        if (!client.isConnected()) {
            client.connect();
        }

        MqttMessage mqttMessage = new MqttMessage(payload);

        mqttMessage.setQos(1);
        mqttMessage.setRetained(false);
        client.publish(topic, mqttMessage);

    }

}