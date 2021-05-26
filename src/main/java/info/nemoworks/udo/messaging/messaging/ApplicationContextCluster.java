package info.nemoworks.udo.messaging.messaging;

import org.javatuples.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContextCluster {

    private static Map<String, Pair<ApplicationContext, Set<String>>> applicationContextMap = new ConcurrentHashMap<>();

    public static Map<String, Pair<ApplicationContext, Set<String>>> getApplicationContextMap() {
        return applicationContextMap;
    }

    public static synchronized Map<String,Pair<ApplicationContext, Set<String>>> createApplicationContext(ApplicationContext applicationContext){
        Set<String> udoIdSet = new HashSet<>();
        Pair<ApplicationContext, Set<String>> pair = new Pair<>(applicationContext, udoIdSet);
        applicationContextMap.put(applicationContext.getAppId(),pair);
        return applicationContextMap;
    }

    public static synchronized Map<String,Pair<ApplicationContext, Set<String>>> removeApplicationContext(String appId){
        applicationContextMap.remove(appId);
        return applicationContextMap;
    }

    public static synchronized Map<String,Pair<ApplicationContext, Set<String>>> addUdoId(String appId,String udoId){
        applicationContextMap.get(appId).getValue1().add(udoId);
        return applicationContextMap;
    }

    public static synchronized Map<String,Pair<ApplicationContext, Set<String>>> removeUdoId(String appId,String udoId){
        applicationContextMap.get(appId).getValue1().remove(udoId);
        return applicationContextMap;
    }
}
