package info.nemoworks.udo.messaging;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MessagingApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(MessagingApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {


	}

	@Bean
	public IMqttClient mqttClient() throws MqttException {
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