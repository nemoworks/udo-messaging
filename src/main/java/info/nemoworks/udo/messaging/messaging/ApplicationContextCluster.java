package info.nemoworks.udo.messaging.messaging;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.javatuples.Pair;

//@Component
public class ApplicationContextCluster {

//    @Autowired
//    UdoService udoService;
//
//    @Autowired
//    EventBus eventBus;
//
//    @Autowired
//    HTTPServiceGateway httpServiceGateway;
//
//    @Autowired
//    MQTTGateway mqttGateway;

    private static Map<String, Pair<ApplicationContext, Set<String>>> applicationContextMap = new ConcurrentHashMap<>();

    public static Map<String, Pair<ApplicationContext, Set<String>>> getApplicationContextMap() {
        return applicationContextMap;
    }

//    public ApplicationContext createApplicationContext(Udo udo) throws MqttException, IOException {
//        String clientid1 = UUID.randomUUID().toString();
//        MqttClient client1 = new MqttClient("tcp://test.mosquitto.org:1883", clientid1);
//        MqttConnectOptions options = new MqttConnectOptions();
//        options.setAutomaticReconnect(true);
//        options.setCleanSession(true);
//        options.setConnectionTimeout(10);
//        client1.connect(options);
//        Publisher httpPublisher = new Publisher(client1);
//
//        String clientid2 = UUID.randomUUID().toString();
//        MqttClient client2 = new MqttClient("tcp://test.mosquitto.org:1883", clientid2);
//        client2.connect(options);
//        Subscriber httpSubscriber = new Subscriber(client2);
//
//        String clientid3 = UUID.randomUUID().toString();
//        MqttClient client3 = new MqttClient("tcp://broker.emqx.io:1883", clientid3);
//        String clientid4 = UUID.randomUUID().toString();
//        MqttClient client4 = new MqttClient("tcp://broker.emqx.io:1883", clientid4);
//        client3.connect(options);
//        client4.connect(options);
//        Publisher mqttPublisher = new Publisher(client3);
//        Subscriber mqttSubscriber = new Subscriber(client4);
//        ApplicationContext applicationContext = new ApplicationContext(httpPublisher,
//            httpSubscriber,
//            mqttPublisher, mqttSubscriber,
//            httpServiceGateway, mqttGateway);
//        applicationContext.setAppId(udo.getId());
//        eventBus.register(applicationContext);
//        return applicationContext;
//    }

    public static synchronized Map<String, Pair<ApplicationContext, Set<String>>> createApplicationContext(
        ApplicationContext applicationContext) {
        Set<String> udoIdSet = new HashSet<>();
        Pair<ApplicationContext, Set<String>> pair = new Pair<>(applicationContext, udoIdSet);
        applicationContextMap.put(applicationContext.getAppId(), pair);
        return applicationContextMap;
    }

    public static synchronized Map<String, Pair<ApplicationContext, Set<String>>> removeApplicationContext(
        String appId) {
        applicationContextMap.remove(appId);
        return applicationContextMap;
    }

    public static synchronized Map<String, Pair<ApplicationContext, Set<String>>> addUdoId(
        String appId, String udoId) {
        applicationContextMap.get(appId).getValue1().add(udoId);
        return applicationContextMap;
    }

    public static synchronized Map<String, Pair<ApplicationContext, Set<String>>> removeUdoId(
        String appId, String udoId) {
        applicationContextMap.get(appId).getValue1().remove(udoId);
        return applicationContextMap;
    }
}
