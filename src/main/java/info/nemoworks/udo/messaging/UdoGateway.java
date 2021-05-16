package info.nemoworks.udo.messaging;

import java.util.Set;

import info.nemoworks.udo.model.Udo;

public abstract class UdoGateway {

    private Set<Udo> udos;

    public boolean forUdo(Udo udo){
        return this.udos.contains(udo);
    }

    protected MessagingManager messagingManager;

    public UdoGateway(MessagingManager messagingManager) {
        this.messagingManager = messagingManager;
    }

    //calling the service/device
    public abstract void downlink(String appId, Udo udo, byte[] payload);

    //messaging back to manager
    protected void uplink(String appId, Udo udo, byte[] payload) {
        this.messagingManager.handleUplink(appId, udo, payload);
    }

}
