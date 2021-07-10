package info.nemoworks.udo.messaging.messaging;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.gateway.MQTTGateway;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UriType;
import info.nemoworks.udo.model.event.GatewayEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContext {

    private String appId;

    private Publisher httpPublisher;

    private Subscriber httpSubscriber;

    private Publisher mqttPublisher;

    private Subscriber mqttSubscriber;

    private final HTTPServiceGateway httpServiceGateway;

    private final MQTTGateway mqttGateway;

    private FilterRule filterRule;

    private static final String PREFIX_SUB = "sub";
    private static final String PREFIX_PUB = "pub";
    private static final String PREFIX_MsgManager = "app_";

    public ApplicationContext(Publisher httpPublisher, Subscriber httpSubscriber,
        Publisher mqttPublisher, Subscriber mqttSubscriber,
        HTTPServiceGateway httpServiceGateway,
        MQTTGateway mqttGateway)
        throws IOException {
        this.mqttPublisher = mqttPublisher;
        this.mqttSubscriber = mqttSubscriber;
        this.httpPublisher = httpPublisher;
        this.httpSubscriber = httpSubscriber;
        this.httpServiceGateway = httpServiceGateway;
        this.mqttGateway = mqttGateway;
        this.filterRule = new FilterRule(new String(
            Files.readAllBytes(Paths.get("udo-messaging/src/resources/testRules.json"))));
    }

    public void setAppId(String appId) {
        this.appId = PREFIX_MsgManager + appId;
        ApplicationContextCluster.createApplicationContext(this);
    }

    // <pub_topic, sub_topic>
    public static Pair<String, String> getMqttTopic(String appId, String udoId) {
        return new Pair<>(PREFIX_PUB + "/" + appId + "/" + udoId
            , PREFIX_SUB + "/" + appId + "/" + udoId);
    }

    private String getUdoId(String topic) {
        return topic.split("/")[2];
    }


    @Subscribe
    public void ackMessage(GatewayEvent gatewayEvent) {
        Udo udo = (Udo) gatewayEvent.getSource();
//        if(!filterRule.isEqual(filterRule)){
//            return;
//        }
        if (udo.getUri().getUriType().equals(UriType.HTTP)) {
            if (!filterRule.filteringEqual(udo)) {
                System.out.println("Negative to filter Equal! " + "Udo Id: " + udo.getId());
            } else if (!filterRule.filteringLarger(udo)) {
                System.out.println("Negative to filter Larger! " + "Udo Id: " + udo.getId());
            }
            ApplicationContextCluster.getApplicationContextMap().get("app_demo").getValue1()
                .forEach(udoId -> {
                    if (!udo.getId().equals(udoId)) {
                        String topic = getMqttTopic("app_demo", udoId).getValue1();
                        Thread thread = Thread.currentThread();
                        System.out.println("thread=====" + thread.getId());
                        this.publishMessage(topic, udo.getData().toString().getBytes());
                    }
                });
        }

    }

    public void publishMessage(String topic, byte[] payload) {
        try {
            System.out.println(("publish========" + ":" + new String(payload)));
            this.httpPublisher.publish(topic, payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribeMessage(String appId, Udo udo) throws MqttException {
        if (!ApplicationContextCluster.getApplicationContextMap().get(appId)
            .contains(udo.getId())) {
            ApplicationContextCluster.addUdoId(appId, udo.getId());
        }
        if (udo.getUri().getUriType().equals(UriType.HTTP)) {
            httpSubscriber
                .subscribe(getMqttTopic(appId, udo.getId()).getValue1(), (topic, payload) -> {
                    String udoId = getUdoId(topic);
                    payload.getPayload();
                    Thread thread = Thread.currentThread();
                    System.out.println("thread=====" + thread.getId());
                    System.out.println("httpSubscriber=====" + new String(payload.getPayload()));
                    Gson gson = new Gson();
                    try {
                        JsonObject data = gson
                            .fromJson(new String(payload.getPayload()), JsonObject.class);
                        httpServiceGateway
                            .updateLink(udoId, udo.getUri().getUri().getBytes(), gson.toJson(data));
                    } catch (Exception e) {
                        System.out.println("Data is not in the Form of JSON!");
                    }
                });
        } else {
            mqttSubscriber
                .subscribe("sub/topic/test", (topic, payload) -> {
                    payload.getPayload();
                    Thread thread = Thread.currentThread();
                    System.out.println("thread=====" + thread.getId());
                    System.out.println("mqttSubscriber=====" + new String(payload.getPayload()));
                    Gson gson = new Gson();
                    try {
                        JsonObject data = gson
                            .fromJson(new String(payload.getPayload()), JsonObject.class);
                        mqttGateway
                            .updateLink(topic, udo.getUri().getUri().getBytes(), gson.toJson(data));
                    } catch (Exception e) {
                        System.out.println("Data is not in the Form of JSON!");
                    }
                });
        }

    }

    public String getAppId() {
        return appId;
    }

    public FilterRule getFilterRule() {
        return filterRule;
    }

    public void setFilterRule(FilterRule filterRule) {
        this.filterRule = filterRule;
    }
}
