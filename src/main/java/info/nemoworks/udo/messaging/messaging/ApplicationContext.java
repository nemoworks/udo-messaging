package info.nemoworks.udo.messaging.messaging;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.gateway.UdoGateway;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.event.GatewayEvent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationContext {

    private String appId;

    private Publisher publisher;

    private Subscriber subscriber;

    private final UdoGateway udoGateway;

    private FilterRule filterRule;

    private static final String PREFIX_SUB = "sub";
    private static final String PREFIX_PUB = "pub";
    private static final String PREFIX_MsgManager = "app_";

    public ApplicationContext(Publisher publisher, Subscriber subscriber, UdoGateway udoGateway) {
        this.publisher = publisher;
        this.subscriber = subscriber;
        this.udoGateway = udoGateway;
    }

    public void setAppId(String appId) {
        this.appId = PREFIX_MsgManager+appId;
        ApplicationContextCluster.createApplicationContext(this);
    }

    // <pub_topic, sub_topic>
    public static Pair<String, String> getMqttTopic(String appId, String udoId) {
        return new Pair<>(PREFIX_PUB + "/" + appId + "/" + udoId
                , PREFIX_SUB + "/" + appId+ "/" + udoId);
    }

    private String getUdoId(String topic) {
        return topic.split("/")[2];
    }


    @Subscribe
    public void ackMessage(GatewayEvent gatewayEvent){
        Udo udo = (Udo) gatewayEvent.getSource();
//        if(!filterRule.isEqual(filterRule)){
//            return;
//        }
        ApplicationContextCluster.getApplicationContextMap().get("app_demo").getValue1().forEach(udoId->{
//            if(!udo.getId().equals(udoId)){
                String topic = getMqttTopic("app_demo", udoId).getValue1();
                Thread thread = Thread.currentThread();
                System.out.println("thread====="+thread.getId());
                this.publishMessage(topic,udo.getData().toString().getBytes());
//            }
        });

    }

    public void publishMessage(String topic, byte[] payload) {
        try {
            System.out.println(("publish========" + ":" + new String(payload)));
            this.publisher.publish(topic, payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribeMessage(String appId, Udo udo) throws MqttException {
        if (!ApplicationContextCluster.getApplicationContextMap().get(appId).contains(udo.getId())) {
            ApplicationContextCluster.addUdoId(appId, udo.getId());
        }
        subscriber.subscribe(getMqttTopic(appId, udo.getId()).getValue1(), (topic, payload) -> {

            String udoId = getUdoId(topic);
            payload.getPayload();
            Thread thread = Thread.currentThread();
            System.out.println("thread====="+thread.getId());
            System.out.println("subscriber=====" + new String(payload.getPayload()));
            JsonObject data = new Gson().fromJson(new String(payload.getPayload()), JsonObject.class);
            HashMap<Object, Object> hashMap = new Gson().fromJson(data.toString(), HashMap.class);
            udoGateway.updateLink(udoId, udo.getUri().getBytes(),hashMap);
        });
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
