package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.ContextInfo;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.Uri;
import info.nemoworks.udo.model.UriType;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.SaveByUriEvent;
import info.nemoworks.udo.model.event.SyncEvent;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class UdoGateway {

    @Autowired
    EventBus eventBus;

//    private static final Logger logger = LoggerFactory.getLogger(UdoGateway.class);

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

    protected UdoGateway() throws IOException {
//        this.filterRuleMap = new HashMap<>();
    }


    // pulling msg from the service/device
    public abstract void downLink(String tag, byte[] payload)
        throws IOException, InterruptedException;

    public abstract void updateLink(String tag, byte[] payload, String data)
        throws IOException, InterruptedException;


    //upadte udo by polling
    public void updateUdoByPolling(String tag, byte[] payload) {
        log.info("fetch Udo " + new String(payload) + "try to update");
        Udo udo = this.updateUdo(tag, payload);
        eventBus.post(new SyncEvent(EventType.SYNC, udo, null));
    }

    //update udo by uri
    protected void updateUdoByUri(String tag, byte[] payload, byte[] uri, ContextInfo contextInfo,
        UriType uriType) {
        Udo udo = this.updateUdo(tag, payload);
        udo.setUri(new Uri(new String(uri), uriType));
        udo.setContextInfo(contextInfo);
        eventBus.post(new SaveByUriEvent(EventType.SAVE_BY_URI, udo, uri));
    }

    public Udo updateUdo(String id, byte[] payload) {
        JsonObject data = new Gson().fromJson(new String(payload), JsonObject.class);
        if (data != null) {
            if (data.has("last_updated")) {
                data.remove("last_updated");
            }
            if (data.has("last_changed")) {
                data.remove("last_changed");
            }
            if (data.has("attributes")) {
                JsonObject attributes = data.get("attributes").getAsJsonObject();
                if (attributes.has("speed_list")) {
                    attributes.remove("speed_list");
                }
                if (attributes.has("preset_modes")) {
                    attributes.remove("preset_modes");
                }
                if (attributes.has("preset_mode")) {
                    attributes.remove("preset_mode");
                }
                if (attributes.has("speed")) {
                    attributes.remove("speed");
                }
                if (attributes.has("button_pressed")) {
                    attributes.remove("button_pressed");
                }
                data.remove("attributes");
                data.add("attributes", attributes);
            }
        }
        Udo udo = new Udo(null, data);
        udo.setId(id);
        return udo;
    }
}
