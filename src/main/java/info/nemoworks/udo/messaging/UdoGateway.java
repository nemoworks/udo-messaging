package info.nemoworks.udo.messaging;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.SyncEvent;
import info.nemoworks.udo.model.Udo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public abstract class UdoGateway {

//    protected MessagingManager messagingManager;
    @Autowired
    EventBus eventBus;

    public enum UdoGatewayType {
        HTTP,MQTT
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

    // messaging back to manager
//    protected void uplink(String tag, byte[] payload) {
//        this.messagingManager.handleUplink(tag, payload);
//    }

    //upadte udo
    protected void updateUdo(String tag,byte[] payload){
        String s = "{'Name':'Jeep'}";
        JsonObject data = new Gson().fromJson(new String(payload),JsonObject.class);
        Udo udo = new Udo(null,data);
        udo.setId(tag);
        eventBus.post(new SyncEvent("sync",udo));
//        Udo udo = udoService.getUdoById(tag);
//        udo.setData(data);
//        try {
//            udoService.saveOrUpdateUdo(udo);
//        } catch (UdoServiceException e) {
//            e.printStackTrace();
//        }
    }

}
