//package info.nemoworks.udo.messaging;
//
//import org.eclipse.paho.client.mqttv3.IMqttClient;
//import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.springframework.stereotype.Component;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//public class Subscriber {
//
//    private IMqttClient client;
//
//    public Subscriber(IMqttClient client) {
//        this.client = client;
//    }
//
//    public void subscribe(String mqttTopic, IMqttMessageListener listener) throws MqttException {
//        if (!client.isConnected()) {
//            client.connect();
//            log.debug("client" + client.getClientId() + "connected");
//        }
//
//        client.subscribe(mqttTopic, listener);
//    }
//
//}
