package info.nemoworks.udo.messaging;

import java.util.UUID;

import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.gateway.UdoGateway;
import info.nemoworks.udo.messaging.messaging.ApplicationContext;
import info.nemoworks.udo.messaging.messaging.Publisher;
import info.nemoworks.udo.messaging.messaging.Subscriber;
import info.nemoworks.udo.model.Udo;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PubSubTest {

    MqttClient client1, client2,client3;

    UdoGateway udoGateway;
    ApplicationContext applicationContext;

    EventBus eventBus;

    @BeforeEach
    public void setup() throws MqttException {

        String clientid1 = UUID.randomUUID().toString();
        client1 = new MqttClient("tcp://test.mosquitto.org:1883", clientid1);

        String clientid2 = UUID.randomUUID().toString();
        client2 = new MqttClient("tcp://test.mosquitto.org:1883", clientid2);

        String clientid3 = UUID.randomUUID().toString();
        client3 = new MqttClient("tcp://test.mosquitto.org:1883", clientid3);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        client1.connect(options);
        client2.connect(options);
        client3.connect(options);
        eventBus = new EventBus();

        udoGateway = new HTTPServiceGateway();
        eventBus.register(udoGateway);

    }

    @Test
    public void whensubandpub_thenSuccess() throws MqttException, InterruptedException {

         Subscriber subscriber = new Subscriber(client1);
         Udo udo = new Udo(null, null);

         subscriber.subscribe("udo",(topic, payload) -> {
             System.out.println("subscriber====="+new String(payload.getPayload()));
         });

        subscriber.subscribe("udo1",(topic, payload) -> {
            System.out.println("subscriber====="+new String(payload.getPayload()));
        });
        Publisher publisher = new Publisher(client2);
        Publisher publisher1 = new Publisher(client3);
         udo.setId(UUID.randomUUID().toString());

         int i = 0;
         while(i<10){
             publisher.publish("udo", "hello udo".getBytes());
             publisher1.publish("udo", "hello udo11".getBytes());
             Thread.sleep(1000);
             i++;
         }
         client1.close();
         client2.close();


    }


    @Test
    public void test_ApplicationContext_Pub_Sub() throws MqttException {
        Publisher publisher = new Publisher(client2);
        Subscriber subscriber = new Subscriber(client1);
        applicationContext = new ApplicationContext(publisher,subscriber, udoGateway);
        applicationContext.setAppId("demo");
        eventBus.register(applicationContext);
        Udo udo = new Udo(null, null);
        udo.setId(UUID.randomUUID().toString());
        Pair<String, String> mqttTopic = applicationContext.getMqttTopic(applicationContext.getAppId(), udo.getId());
        applicationContext.subscribeMessage(applicationContext.getAppId(), udo);
        applicationContext.publishMessage(mqttTopic.getValue1(), "asasasaxcasdcswd".getBytes());
    }
}
