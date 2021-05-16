package info.nemoworks.udo.messaging;

import java.util.List;
import java.util.Map;

import info.nemoworks.udo.model.Udo;

public abstract class UdoGateway {

    private Map<String, List<Udo>> udoinapp;

    protected MessagingManager messagingManager;

    public UdoGateway(MessagingManager messagingManager) {
        this.messagingManager = messagingManager;
    }

    //calling the service/device
    public abstract void downlink(String appId, Udo udo, byte[] payload);

    //messaging back to manager
    protected void uplink(String appId, Udo udo, byte[] payload) {
        this.messagingManager.publish(appId, udo, payload);
    }

}
