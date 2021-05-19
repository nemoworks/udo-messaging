package info.nemoworks.udo.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.service.UdoService;
import info.nemoworks.udo.service.UdoServiceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;

public abstract class UdoGateway {

    protected MessagingManager messagingManager;

    @Autowired
    UdoService udoService;

    protected UdoGateway(MessagingManager messagingManager) {
        this.messagingManager = messagingManager;
    }

    // calling the service/device
    public abstract void downlink(String tag, byte[] payload) throws IOException, InterruptedException;

    // messaging back to manager
    protected void uplink(String tag, byte[] payload) {
        this.messagingManager.handleUplink(tag, payload);
    }

    //upadte udo
    protected void updateUdo(String tag,byte[] payload){
        JsonObject data = new Gson().fromJson(Arrays.toString(payload),JsonObject.class);
        Udo udo = udoService.getUdoById(tag);
        udo.setData(data);
        try {
            udoService.saveOrUpdateUdo(udo);
        } catch (UdoServiceException e) {
            e.printStackTrace();
        }
    }

}
