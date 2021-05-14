package info.nemoworks.udo.messaging;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import info.nemoworks.udo.model.Udo;

public class Publisher {

    public static final String TOPIC_PREFIX = "udo/";

    private IMqttClient client;

    public Publisher(IMqttClient client) {
        this.client = client;
    }

    public Void publish(Udo udo, String message) throws MqttException {
        if (!client.isConnected()) {
            client.connect();
        }

        byte[] payload = message.getBytes();

        MqttMessage mqttMessage = new MqttMessage(payload);

        mqttMessage.setQos(1);
        mqttMessage.setRetained(true);
        client.publish(TOPIC_PREFIX + udo.getId(), mqttMessage);

        return null;
    }

}