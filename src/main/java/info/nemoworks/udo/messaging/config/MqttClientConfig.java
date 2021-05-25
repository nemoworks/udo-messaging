package info.nemoworks.udo.messaging.config;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MqttClientConfig {
	@Bean
	public MqttClient mqttClient() throws MqttException {
		String clientid = UUID.randomUUID().toString();
		MqttClient client = null;
		client = new MqttClient("tcp://test.mosquitto.org:1883", clientid);
		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		client.connect(options);
		return client;
	}
}
