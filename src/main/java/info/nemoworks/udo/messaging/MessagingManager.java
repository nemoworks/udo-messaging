//package info.nemoworks.udo.messaging;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//
//import com.google.common.eventbus.EventBus;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.javatuples.Pair;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import info.nemoworks.udo.model.Udo;
//
//public class MessagingManager {
//
//    @Autowired
//    private Publisher publisher;
//
//    @Autowired
//    private Subscriber subscriber;
//
////    @Autowired
////    private EventBus eventBus;
//
//    private Set<UdoGateway> gateways;
//
//    private static final String PREFIX_UDO = "udo";
//
//    public MessagingManager() {
//       // eventBus.register(this);
//        this.gateways = new HashSet<>();
//    }
//
//    // <pub_topic, sub_topic>
//    private Pair<String, String> getMqttTopic(String appId, Udo udo) {
//        return new Pair<>(PREFIX_UDO + "/" + "app" + appId + "/" + udo.getId(), PREFIX_UDO + "/" + "app" + appId);
//    }
//
//    public void registerUdoInAppContext(Udo udo, String appId) throws MqttException {
//
//        udo.getContextInfo().addContext(appId, getMqttTopic(appId, udo));
//
//
//        subscriber.subscribe(getMqttTopic(appId, udo).getValue1(), (topic, payload) -> {
//             for (UdoGateway udoGateway : gateways) {
//                 switch (udoGateway.getType()){
//                     case HTTP:
//                         HTTPServiceGateway gateway = (HTTPServiceGateway) udoGateway;
//                         gateway.getEndpoints().forEach((s, uri) -> {
//                             try {
//                                 udoGateway.downlink(udo.getId(), payload.getPayload());
//                             } catch (IOException e) {
//                                 e.printStackTrace();
//                             } catch (InterruptedException e) {
//                                 e.printStackTrace();
//                             }
//                         });
//                         break;
//                     case MQTT:
//                         System.out.println("mqtt Service Gateway");
//                         break;
//                 }
//
//             }
//        });
//    }
//
////    public synchronized void handleUplink(String tag, byte[] payload) {
////
////         try {
////            System.out.println(tag + ":" + new String(payload));
////             this.publisher.publish(getMqttTopic(appId, udo).getValue0(), payload);
////         } catch (MqttException e) {
////             e.printStackTrace();
////         }
////    }
//
////    @Subscribe
////    public synchronized void handleUdoEvent(UdoEvent event) {
////        try {
////            this.publisher.publish(event.getSource().getContextInfo().getContext(event.getContextId()).toString(),
////                    event.getPayload());
////        } catch (MqttException e) {
////            e.printStackTrace();
////        }
////
////    }
//
//    public Set<UdoGateway> getGateways() {
//        return gateways;
//    }
//
//    public void registerGateway(UdoGateway udoGateway) {
//        this.gateways.add(udoGateway);
//    }
//
//}
