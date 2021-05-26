package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.SaveByMqttEvent;
import info.nemoworks.udo.model.event.SaveByUriEvent;
import info.nemoworks.udo.model.event.SyncEvent;
import info.nemoworks.udo.model.Udo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;


public abstract class UdoGateway {

    @Autowired
    EventBus eventBus;

    public enum UdoGatewayType {
        HTTP, MQTT
    }

    public UdoGatewayType getType() {
        return type;
    }

    public void setType(UdoGatewayType type) {
        this.type = type;
    }

    private UdoGatewayType type;

    protected UdoGateway() {
    }

    // calling the service/device
    public abstract void downlink(String tag, byte[] payload) throws IOException, InterruptedException;

    //upadte udo by polling
    public void updateUdoByPolling(String tag, byte[] payload) {
        System.out.println("update Udo " + payload.toString());
        Udo udo = this.updateUdo(tag, payload);
        eventBus.post(new SyncEvent(EventType.SYNC, udo, null));
    }

    //update udo by uri
    protected void updateUdoByUri(String tag, byte[] payload, byte[] uri) {
        Udo udo = this.updateUdo(tag, payload);
        udo.setUri(new String(uri));
        eventBus.post(new SaveByUriEvent(udo, null, EventType.SAVE_BY_URI, URI.create(new String(uri))));
    }

    public void updateUdoByMqtt(String tag, byte[] payload) {
        Udo udo = this.updateUdo(tag, payload);
        eventBus.post(new SaveByMqttEvent(EventType.SAVE_BY_MQTT, udo, null));
    }

    public Udo updateUdo(String id, byte[] payload) {
        JsonObject data = new Gson().fromJson(new String(payload), JsonObject.class);
        Udo udo = new Udo(null, data);
        udo.setId(id);
        return udo;
    }

}
