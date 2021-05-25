package info.nemoworks.udo.messaging.messaging;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContextCluster {

    private static Map<String, Set<String>> applicationContextMap = new ConcurrentHashMap<>();

    public static Map<String, Set<String>> getApplicationContextMap() {
        return applicationContextMap;
    }

    public static synchronized Map<String,Set<String>> createApplicationContext(String appId){
        Set<String> udoIdSet = new HashSet<>();
        applicationContextMap.put(appId,udoIdSet);
        return applicationContextMap;
    }

    public static synchronized Map<String,Set<String>> removeApplicationContext(String appId){
        applicationContextMap.remove(appId);
        return applicationContextMap;
    }

    public static synchronized Map<String,Set<String>> addUdoId(String appId,String udoId){
        applicationContextMap.get(appId).add(udoId);
        return applicationContextMap;
    }

    public static synchronized Map<String,Set<String>> removeUdoId(String appId,String udoId){
        applicationContextMap.get(appId).remove(udoId);
        return applicationContextMap;
    }
}
