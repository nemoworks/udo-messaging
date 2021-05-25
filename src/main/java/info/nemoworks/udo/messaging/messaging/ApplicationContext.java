package info.nemoworks.udo.messaging.messaging;

import info.nemoworks.udo.model.Udo;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApplicationContext {

    private final String mmid;

    private final Publisher publisher;

    private final Subscriber subscriber;

    private static final String PREFIX_UDO = "udo";
    private static final String PREFIX_MsgManager = "MM_";

    public ApplicationContext(Publisher publisher, Subscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
        this.mmid = PREFIX_MsgManager+ UUID.randomUUID().toString();
    }

    // <pub_topic, sub_topic>
    private Pair<String, String> getMqttTopic(String appId, Udo udo) {
        return new Pair<>(PREFIX_UDO + "/" + "app" + appId + "/" + udo.getId(), PREFIX_UDO + "/" + "app" + appId);
    }

    public synchronized void publishMessage(String topic, byte[] payload) {
         try {
            System.out.println(topic + ":" + new String(payload));
             this.publisher.publish(topic, payload);
         } catch (MqttException e) {
             e.printStackTrace();
         }
    }

    public synchronized void subscribeMessage(String appId,Udo udo) throws MqttException {
        if(!ApplicationContextCluster.getApplicationContextMap().get(appId).contains(udo.getId())){
            ApplicationContextCluster.addUdoId(appId,udo.getId());
        }
        subscriber.subscribe(getMqttTopic(appId,udo).getValue1(),(topic, payload) -> {
            System.out.println("subscriber====="+new String(payload.getPayload()));
        });

    }

    public String getMmid() {
        return mmid;
    }
}
