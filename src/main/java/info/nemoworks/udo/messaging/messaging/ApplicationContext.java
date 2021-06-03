package info.nemoworks.udo.messaging.messaging;

import com.google.common.eventbus.Subscribe;
import info.nemoworks.udo.messaging.gateway.UdoGateway;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.GatewayEvent;
import info.nemoworks.udo.model.event.PublishByMqttEvent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContext {

    private String appId;

    private final Publisher publisher;

    private final Subscriber subscriber;

    private final UdoGateway udoGateway;

    private static final String PREFIX_SUB = "sub";
    private static final String PREFIX_PUB = "sub";
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
    public Pair<String, String> getMqttTopic(String appId, Udo udo) {
        return new Pair<>(PREFIX_PUB + "/" + appId + "/" + udo.getId()
                , PREFIX_SUB + "/" + appId + "/" + udo.getId());
    }

    private String getUdoId(String topic) {
        return topic.split("/")[2];
    }



    @Subscribe
    public void ackMessage(PublishByMqttEvent publishByMqttEvent){
        Udo udo = (Udo) publishByMqttEvent.getSource();
        String topic = getMqttTopic(appId, udo).getValue0();
        this.publishMessage(topic,udo.toString().getBytes());
    }

    public synchronized void publishMessage(String topic, byte[] payload) {
        try {
            System.out.println((topic + ":" + new String(payload)));
            this.publisher.publish(topic, payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribeMessage(String appId, Udo udo) throws MqttException {
        if (!ApplicationContextCluster.getApplicationContextMap().get(appId).contains(udo.getId())) {
            ApplicationContextCluster.addUdoId(appId, udo.getId());
        }
        subscriber.subscribe(getMqttTopic(appId, udo).getValue1(), (topic, payload) -> {
            String udoId = getUdoId(topic);
            payload.getPayload();
           udoGateway.updateUdoByMqtt(udoId, payload.getPayload());
            System.out.println("subscriber=====" + new String(payload.getPayload()));
        });
    }

    public String getAppId() {
        return appId;
    }

}
