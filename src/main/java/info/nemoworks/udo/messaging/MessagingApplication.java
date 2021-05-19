//package info.nemoworks.udo.messaging;
//
//import java.net.URI;
//import java.util.UUID;
//
//import com.google.common.eventbus.EventBus;
//
//import org.eclipse.paho.client.mqttv3.IMqttClient;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//
//import info.nemoworks.udo.model.Udo;
//import info.nemoworks.udo.model.UdoEvent;
//
//@SpringBootApplication
//public class MessagingApplication implements CommandLineRunner {
//
//	@Bean
//	public MessagingManager messagingManager() {
//		return new MessagingManager();
//	}
//
//	public static void main(String[] args) {
//		SpringApplication.run(MessagingApplication.class, args);
//	}
//
//
//	@Autowired
//	private MessagingManager messagingManager;
//
//	@Autowired
//	private HTTPServiceGateway httpServiceGateway;
//
//	@Override
//	public void run(String... args) throws Exception {
//
//		Udo udo = new Udo(null, null);
//		udo.getMetaInfo().uri="http://localhost:8080/actuator/health";
//
//		httpServiceGateway.register(udo.getId(), URI.create(udo.getMetaInfo().uri));
//
//
//		// messagingManager.handleUdoEvent(new UdoEvent("default", source, payload));
//
//	}
//
//	@Bean
//	public EventBus eventBus() {
//		return new EventBus();
//	}
//
//	@Bean
//	public IMqttClient mqttClient() throws MqttException {
//		String clientid = UUID.randomUUID().toString();
//		MqttClient client = null;
//		client = new MqttClient("tcp://test.mosquitto.org:1883", clientid);
//
//		MqttConnectOptions options = new MqttConnectOptions();
//		options.setAutomaticReconnect(true);
//		options.setCleanSession(true);
//		options.setConnectionTimeout(10);
//
//		client.connect(options);
//
//		return client;
//	}
//}