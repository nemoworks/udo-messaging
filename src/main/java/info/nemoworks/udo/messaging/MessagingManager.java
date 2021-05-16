package info.nemoworks.udo.messaging;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UdoEvent;

public class MessagingManager {

    @Autowired
    private Publisher publisher;

    @Autowired
    private Subscriber subscriber;

    @Autowired
    private EventBus eventBus;

    private static final String PREFIX_UDO = "udo";

    public MessagingManager() {
        eventBus.register(this);
    }

    // <pub_topic, sub_topic>
    private Pair<String, String> getMqttTopic(String appId, Udo udo) {

        return new Pair<>(PREFIX_UDO + "/" + "app" + appId + "/" + udo.getId(), PREFIX_UDO + "/" + "app" + appId);
    }

    public void registerUdoInAppContext(Udo udo, String appId) throws MqttException {
        udo.getContextInfo().addContext(appId, getMqttTopic(appId, udo));
        subscriber.subscribe(getMqttTopic(appId, udo).getValue1());
    }

    @Subscribe
    public void udoEvent(UdoEvent event) {
        try {
            this.publisher.publish(event.getSource().getContextInfo().getContext(event.getContextId()).toString(),
                    event.getPayload());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
