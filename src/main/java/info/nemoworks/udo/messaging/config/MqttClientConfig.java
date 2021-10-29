package info.nemoworks.udo.messaging.config;


import java.util.UUID;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MqttClientConfig {

    @Bean
    @Scope("prototype")
    public MqttClient mqttClient() throws MqttException {
        String clientid = UUID.randomUUID().toString();
        MqttClient client = null;
        client = new MqttClient("tcp://210.28.132.168:30609", clientid);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("udo-user");
        char[] password = "123456".toCharArray();
        options.setPassword(password);
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
        client.connect(options);
        return client;
    }
}
