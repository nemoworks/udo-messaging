package info.nemoworks.udo.messaging.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.gateway.MQTTGateway;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UriType;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.GatewayEvent;
import info.nemoworks.udo.service.UdoService;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
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

    //    @Autowired
    private final UdoService udoService;

    private Map<String, FilterRule> filterRuleMap;

    //    private static final String PREFIX_SUB = "sub";
//    private static final String PREFIX_PUB = "pub";
//    private static final String PREFIX_MsgManager = "app_";
    private static final String PREFIX_TOPIC = "topic";


    public ApplicationContext(Publisher httpPublisher, Subscriber httpSubscriber,
        Publisher mqttPublisher, Subscriber mqttSubscriber,
        HTTPServiceGateway httpServiceGateway,
        MQTTGateway mqttGateway, UdoService udoService)
        throws IOException {
        this.mqttPublisher = mqttPublisher;
        this.mqttSubscriber = mqttSubscriber;
        this.httpPublisher = httpPublisher;
        this.httpSubscriber = httpSubscriber;
        this.httpServiceGateway = httpServiceGateway;
        this.mqttGateway = mqttGateway;
        this.udoService = udoService;
        this.filterRuleMap = new HashMap<>();
    }

    public void setAppId(String appId) {
        this.appId = appId;
        ApplicationContextCluster.createApplicationContext(this);
    }

    // 共用一个Topic进行消息收发
    public static String getMqttTopic(String appId) {
        return PREFIX_TOPIC + "/" + appId;
    }

    public void publishRegisterMessage(String appId, String udoId) throws MqttException {
        Udo udo = udoService.getUdoById(udoId);
        JsonObject content = (JsonObject) udo.getData();
        content.addProperty("uri", udo.getUri().getUri());
        JsonObject payload = new JsonObject();
        payload.add("payload", content);
        payload.addProperty("source", "backend");
        payload.addProperty("destination", udo.getUri().getUri());
        payload.addProperty("context", appId);
        payload.addProperty("category", "update");
        this.mqttPublisher
            .publish("topic/register",
                payload.toString().getBytes());
        System.out.println(("publish To Register========" + ":" + new String(
            payload.toString().getBytes())));
        payload.addProperty("destination", "all");
        for (Pair<ApplicationContext, Set<String>> pair : ApplicationContextCluster
            .getApplicationContextMap().values()) {
            Set<String> udos = pair.getValue1();
            if (udos.contains(udo.getId())) {
                this.mqttPublisher
                    .publish("topic/" + pair.getValue0().appId,
                        payload.toString().getBytes());
                System.out.println(
                    ("publish To Context " + pair.getValue0().appId + "========" + ":" + new String(
                        payload.toString().getBytes())));
            }
        }
    }

    public void publishDeleteMessage(String appId, String udoId) throws MqttException {
        Udo udo = udoService.getUdoById(udoId);
        JsonObject content = (JsonObject) udo.getData();
        content.addProperty("uri", udo.getUri().getUri());
        JsonObject payload = new JsonObject();
        payload.add("payload", content);
        payload.addProperty("source", "backend");
        payload.addProperty("destination", "all");
        payload.addProperty("context", appId);
        payload.addProperty("category", "delete");
        this.mqttPublisher
            .publish(getMqttTopic(appId),
                payload.toString().getBytes());
        System.out.println(("publish To Register========" + ":" + new String(
            payload.toString().getBytes())));
    }

    // 数据库内容更新->发消息给服务
    @Subscribe
    public void ackMessage(GatewayEvent gatewayEvent) throws IOException, InterruptedException {
        Udo udo = (Udo) gatewayEvent.getSource();
        if (gatewayEvent.getContextId().equals(EventType.UPDATE)) {
            for (Pair<ApplicationContext, Set<String>> pair : ApplicationContextCluster
                .getApplicationContextMap().values()) {
                Set<String> udos = pair.getValue1();
                if (udos.contains(udo.getId())) {
                    String topic = getMqttTopic(pair.getValue0().getAppId());
                    Thread thread = Thread.currentThread();
                    System.out.println("thread=====" + thread.getId());
                    Gson gson = new Gson();
                    // HTTP广播
                    for (String id : udos) {
                        Udo target = udoService.getUdoById(id);
                        if (target.getUri().getUriType().equals(UriType.HTTP)
                            && udo.getUri().getUriType().equals(UriType.HTTP)) {
                            httpServiceGateway
                                .updateLink(udo.getId(), udo.getUri().getUri().getBytes(),
                                    gson.toJson(udo.getData()));
                        }
                    }
                    // MQTT广播
                    try {
                        JsonObject content = (JsonObject) udo.getData();
                        content.addProperty("uri", udo.getUri().getUri());
                        System.out.println(("publish========" + ":" + new String(
                            content.toString().getBytes())));
                        JsonObject payload = new JsonObject();
                        payload.add("payload", content);
                        payload.addProperty("source", "backend");
                        payload.addProperty("destination", "all");
                        payload.addProperty("context", appId);
                        payload.addProperty("category", "update");
                        this.mqttPublisher
                            .publish(topic,
                                payload.toString().getBytes());
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    // 收消息
    public synchronized void subscribeMessage(String appId) throws MqttException {
        System.out.println("Subscribing Topic: " + getMqttTopic(appId));
        mqttSubscriber
            .subscribe(getMqttTopic(appId), (topic, payload) -> {
                System.out.println("Again, Print Topic: " + topic);
                Gson gson = new Gson();
                JsonObject data = gson
                    .fromJson(new String(payload.getPayload()), JsonObject.class);
                JsonObject content = data.getAsJsonObject("payload");
                String source = data.getAsJsonPrimitive("source").getAsString();
                String destination = data.getAsJsonPrimitive("destination").getAsString();
                // 收到一个消息来源不是后台的消息，表明是外部服务请求更新资源状态
                if (source.equals("backend")) {
                    System.out.println("Message Comes From Backend, Skip");
                    return;
                }
                ConcurrentHashMap<String, String> endpoints = mqttGateway.getEndpoints();
                AtomicReference<String> target = new AtomicReference<>("");
                AtomicReference<String> from = new AtomicReference<>("");
                endpoints.forEach(
                    (id, uri) -> {
                        if (uri.equals(source)) {
                            from.set(id);
                        }
                        if (uri.equals(destination)) {
                            target.set(id);
                        }
                    }
                );
                System.out.println("Before Check Id...");
                String targetId = target.get();
                String fromId = from.get();
                System.out.println("After Check Id...");
                if (this.filteringMessage(fromId, targetId, content)) {
                    mqttPublisher.publish(destination, content.toString().getBytes());
                } else {
                    String topicFail = getMqttTopic(appId);
                    Udo udo = udoService.getUdoById(targetId);
                    JsonObject contentFail = (JsonObject) udo.getData();
                    contentFail.addProperty("uri", udo.getUri().getUri());
                    System.out.println(("Fail Check, Publish Formal Message===" + ":" + new String(
                        contentFail.toString().getBytes())));
                    JsonObject payloadFail = new JsonObject();
                    payloadFail.add("payload", contentFail);
                    payloadFail.addProperty("source", "backend");
                    payloadFail.addProperty("destination", "all");
                    payloadFail.addProperty("context", appId);
                    payloadFail.addProperty("category", "update");
                    this.mqttPublisher
                        .publish(topicFail,
                            payloadFail.toString().getBytes());
                }
            });
    }

    private boolean filteringMessage(String fromId, String targetId, JsonObject messageContent)
        throws JsonProcessingException {
        System.out.println("In filtering Message...");
        System.out.println("Target Id: " + targetId);
        System.out.println("From Id: " + fromId);
        Udo targetUdo = udoService.getUdoById(targetId);
        System.out.println("Middle Check");
        Udo fromUdo = udoService.getUdoById(fromId);
        if (filterRuleMap.get(fromId) != null) {
            FilterRule filterRule = filterRuleMap.get(fromId);
            if (filterRule.filteringDistance(targetUdo, fromUdo)) {
                System.out.println("Distance Check Between " + fromId + " And "
                    + targetId + " Passed!");
            } else {
                System.out.println("Distance Check Between " + fromId + " And "
                    + targetId + " Failed!");
                return false;
            }
        } else {
            System.out.println("No Distance Filter Rule Found, Message Passed!");
        }
        if (filterRuleMap.get(targetId) != null) {
            FilterRule filterRule = filterRuleMap.get(targetId);
            Udo testTemp = new Udo(messageContent);
            this.addDateSticker(testTemp);
            if (filterRule.filteringAll(testTemp)) {
                System.out.println("Attributes Check On "
                    + targetId + " Passed!");
            } else {
                System.out.println("Attributes Check On "
                    + targetId + " Failed!");
                return false;
            }
        } else {
            System.out.println("No Attributes Filter Rule Found, Message Passed!");
        }
        return true;
    }

    private void addDateSticker(Udo udo) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        udo.setClock(sdf.format(date));
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
    }
}
