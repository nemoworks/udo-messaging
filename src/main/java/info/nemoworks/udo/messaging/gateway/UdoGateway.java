package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.EventType;
import info.nemoworks.udo.model.SaveByUriEvent;
import info.nemoworks.udo.model.SyncEvent;
import info.nemoworks.udo.model.Udo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


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

    //upadte udo
    public void updateUdo(String tag, byte[] payload) {
        JsonObject data = new Gson().fromJson(new String(payload), JsonObject.class);
        Udo udo = new Udo(null, data);
        udo.setId(tag);
        eventBus.post(new SyncEvent(EventType.SYNC, udo));
    }

    protected void updateUdoByUri(String tag, byte[] payload, byte[] uri) {
        JsonObject data = new Gson().fromJson(new String(payload), JsonObject.class);
        Udo udo = new Udo(null, data);
        udo.setId(tag);
        udo.setUri(new String(uri));
        eventBus.post(new SaveByUriEvent(EventType.SAVE_BY_URI, udo, uri));
    }

}
