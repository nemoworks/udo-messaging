package info.nemoworks.udo.messaging.messaging;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.gateway.MQTTGateway;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UriType;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.GatewayEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

//    private FilterRule filterRule;

    private Map<String, FilterRule> filterRuleMap;

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
        this.filterRuleMap = new HashMap<>();
//        this.filterRule = new FilterRule(new String(
//            Files.readAllBytes(Paths.get("udo-messaging/src/resources/testRules.json"))));
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
        if (gatewayEvent.getContextId().equals(EventType.UPDATE)) {
            if (new String(gatewayEvent.getPayload()).equals("reject")) {
                return;
            }
            for (Pair<ApplicationContext, Set<String>> pair : ApplicationContextCluster
                .getApplicationContextMap().values()) {
                Set<String> udos = pair.getValue1();
                if (udos.contains(udo.getId())) {
                    udos.forEach(udoId -> {
                        if (!udo.getId().equals(udoId)) {
                            String topic = getMqttTopic("app_http", udoId).getValue1();
                            Thread thread = Thread.currentThread();
                            System.out.println("thread=====" + thread.getId());
                            this.publishMessage(topic, udo.getData().toString().getBytes());
                            try {
                                JsonObject data = (JsonObject) udo.getData();
                                data.addProperty("uri", udo.getUri().getUri());
                                System.out.println(("publish========" + ":" + new String(
                                    data.toString().getBytes())));
                                this.mqttPublisher
                                    .publish("topic/tosub/" + udoId,
                                        data.toString().getBytes());
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
//        if (udo.getUri().getUriType().equals(UriType.HTTP)) {
//            if (!ApplicationContextCluster.getApplicationContextMap().containsKey("app_http")) {
//                return;
//            }
//            ApplicationContextCluster.getApplicationContextMap().get("app_http").getValue1()
//                .forEach(udoId -> {
//                    if (!udo.getId().equals(udoId)) {
//                        String topic = getMqttTopic("app_http", udoId).getValue1();
//                        Thread thread = Thread.currentThread();
//                        System.out.println("thread=====" + thread.getId());
//                        this.publishMessage(topic, udo.getData().toString().getBytes());
//                    }
//                });
//        } else if (udo.getUri().getUriType().equals(UriType.MQTT)) {
//            if (!gatewayEvent.getContextId().equals(EventType.UPDATE)) {
//                return;
//            }
//            if (!ApplicationContextCluster.getApplicationContextMap().containsKey("app_mqtt")) {
//                return;
//            }
//            ApplicationContextCluster.getApplicationContextMap().get("app_mqtt").getValue1()
//                .forEach(udoId -> {
//                    if (!udo.getId().equals(udoId)) {
//                        String topic = "topic/tosub";
//                        Thread thread = Thread.currentThread();
//                        System.out.println("thread=====" + thread.getId());
//                        try {
//                            System.out.println(("publish========" + ":" + new String(
//                                udo.getData().toString().getBytes())));
//                            this.mqttPublisher.publish(topic, udo.getData().toString().getBytes());
//                        } catch (MqttException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//        }
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
        }
        mqttSubscriber
            .subscribe("topic/tosub/" + udo.getId(), (topic, payload) -> {
                payload.getPayload();
                Thread thread = Thread.currentThread();
                System.out.println("thread=====" + thread.getId());
                System.out.println("mqttSubscriber=====" + new String(payload.getPayload()));
                Gson gson = new Gson();
                try {
                    JsonObject data = gson
                        .fromJson(new String(payload.getPayload()), JsonObject.class);
//                    data.addProperty("uri", udo.getUri().getUri());
                    mqttGateway
                        .updateLink(udo.getUri().getUri(),
                            udo.getUri().getUri().getBytes(),
                            gson.toJson(data));
                } catch (Exception e) {
                    System.out.println("Data is not in the Form of JSON!");
                }
            });


    }

    public String getAppId() {
        return appId;
    }

    public Map<String, FilterRule> getFilterRuleMap() {
        return filterRuleMap;
    }

    public FilterRule getFilterRule(String id) {
        return this.filterRuleMap.get(id);
    }

    public void addFilterRule(String id, FilterRule filterRule) {
        this.filterRuleMap.put(id, filterRule);
        this.httpServiceGateway.addFilterRule(id, filterRule);
        this.mqttGateway.addFilterRule(id, filterRule);
    }
}
