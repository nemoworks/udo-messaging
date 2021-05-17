package info.nemoworks.udo.messaging;

import java.io.IOException;

public abstract class UdoGateway {

    protected MessagingManager messagingManager;

    protected UdoGateway(MessagingManager messagingManager) {
        this.messagingManager = messagingManager;
    }

    // calling the service/device
    public abstract void downlink(String tag, byte[] payload) throws IOException, InterruptedException;

    // messaging back to manager
    protected void uplink(String tag, byte[] payload) {
        this.messagingManager.handleUplink(tag, payload);
    }

}
