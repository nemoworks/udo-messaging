package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.messaging.Publisher;
import info.nemoworks.udo.messaging.messaging.Subscriber;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UriType;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.GatewayEvent;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

@Component
public class MQTTGateway extends UdoGateway {

    private Publisher publisher;

    private Subscriber subscriber;

    public ConcurrentHashMap<String, String> getEndpoints() {
        return endpoints;
    }

    private final ConcurrentHashMap<String, String> endpoints;

    private String topic;

    public MQTTGateway() throws MqttException {
        super();
        String clientid1 = UUID.randomUUID().toString();
        MqttClient client1 = new MqttClient("tcp://broker.emqx.io:1883", clientid1);
        String clientid2 = UUID.randomUUID().toString();
        MqttClient client2 = new MqttClient("tcp://broker.emqx.io:1883", clientid2);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        client1.connect(options);
        client2.connect(options);
        this.publisher = new Publisher(client1);
        this.subscriber = new Subscriber(client2);
        this.endpoints = new ConcurrentHashMap<>();
        this.topic = "topic/test";
    }

    // 向udo发送状态更新请求
    @Override
    public void downLink(String tag, byte[] payload) throws IOException, InterruptedException {
        try {
            subscriber.subscribe(new String(payload), (topic, data) -> {
                data.getPayload();
                Thread thread = Thread.currentThread();
                System.out.println(
                    "MQTT subscribe To Human Service=====" + new String(data.getPayload()));
                Gson gson = new Gson();
                try {
                    JsonObject update = gson
                        .fromJson(new String(data.getPayload()), JsonObject.class);
                    this.updateUdoByPolling(tag, gson.toJson(update).getBytes());
                } catch (Exception e) {
                    System.out.println("Data is not in the Form of JSON!");
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 轮询监听资源状态
    public void start() throws InterruptedException {
//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//
//        executor.scheduleWithFixedDelay(
//            () -> {
        endpoints.forEach(
            (key, value) -> {
                try {
                    this.downLink(key, value.getBytes());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        );
//            },
//            15L,
//            15L,
//            TimeUnit.SECONDS);
//        TimeUnit.SECONDS.sleep(15L);
    }

    @Subscribe
    public void subscribeMessage(GatewayEvent gatewayEvent) {
        Udo udo = (Udo) gatewayEvent.getSource();
        if (udo.getUri().getUriType().equals(UriType.HTTP)) {
            return;
        }
        EventType contextId = gatewayEvent.getContextId();
        switch (contextId) {
            case SAVE:
                this.register(udo.getId(), this.topic);
                break;
            case UPDATE:
//                    this.updateLink(this.topic, new Gson().toJson(udo.getData()).getBytes(),
//                        udo.getData().toString());
                break;
            case DELETE:
                this.unregister(udo.getId(), this.topic);
                break;
            default:
                break;
        }

    }

    public synchronized void register(String udoi, String topic) {
        if (!endpoints.containsKey(udoi)) {
            endpoints.put(udoi, topic);
        }
    }

    public synchronized void unregister(String udoi, String topic) {
        if (endpoints.containsKey(udoi)) {
            endpoints.remove(udoi, topic);
        }
    }

    private String getUdoId(String topic) {
        return topic.split("/")[2];
    }

    // 向资源发送状态更新请求
    @Override
    public void updateLink(String topic, byte[] payload, String data)
        throws IOException, InterruptedException {
        try {
            System.out
                .println(("MQTT publish To Human Resource========" + ":" + new String(data)));
            this.publisher.publish(topic, data.getBytes());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}
