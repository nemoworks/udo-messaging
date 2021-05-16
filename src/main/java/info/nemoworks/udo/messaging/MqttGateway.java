package info.nemoworks.udo.messaging;

import info.nemoworks.udo.model.Udo;

public class MqttGateway extends UdoGateway {

    public MqttGateway(MessagingManager messagingManager) {
        super(messagingManager);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void downlink(String appId, Udo udo, byte[] payload) {
        // TODO Auto-generated method stub
        
    }

}
