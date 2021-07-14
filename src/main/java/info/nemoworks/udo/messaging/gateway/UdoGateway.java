package info.nemoworks.udo.messaging.gateway;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.messaging.FilterRule;
import info.nemoworks.udo.model.ContextInfo;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.Uri;
import info.nemoworks.udo.model.UriType;
import info.nemoworks.udo.model.event.EventType;
import info.nemoworks.udo.model.event.SaveByUriEvent;
import info.nemoworks.udo.model.event.SyncEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class UdoGateway {

    @Autowired
    EventBus eventBus;

//    private static final Logger logger = LoggerFactory.getLogger(UdoGateway.class);

    public enum UdoGatewayType {
        HTTP, MQTT
    }

    private Map<String, FilterRule> filterRuleMap;

//    private FilterRule filterRule;

    public UdoGatewayType getType() {
        return type;
    }

    public void setType(UdoGatewayType type) {
        this.type = type;
    }

    private UdoGatewayType type;

    protected UdoGateway() throws IOException {
        this.filterRuleMap = new HashMap<>();
    }

//    public void setFilterRule(FilterRule filterRule) {
//        this.filterRule = filterRule;
//    }

//    public FilterRule getFilterRule() {
//        return this.filterRule;
//    }

    public void addFilterRule(String id, FilterRule filterRule) {
        this.filterRuleMap.put(id, filterRule);
    }

    // pulling msg from the service/device
    public abstract void downLink(String tag, byte[] payload)
        throws IOException, InterruptedException;

    public abstract void updateLink(String tag, byte[] payload, String data)
        throws IOException, InterruptedException;

    public boolean filteringUdo(Udo udo) {
        FilterRule filterRule = this.filterRuleMap.get(udo.getId());
//        return filterRule.filteringTimerLess(udo) && filterRule.filteringTimerLarger(udo);
        return filterRule.filteringAll(udo);
    }

    private Udo addDateSticker(Udo udo) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        udo.setClock(sdf.format(date));
        return udo;
    }

    //upadte udo by polling
    public void updateUdoByPolling(String tag, byte[] payload) {
        log.info("fetch Udo " + new String(payload) + "try to update");
        Udo udo = this.addDateSticker(this.updateUdo(tag, payload));
        // record modify time (for filtering)
//        udo = this.addDateSticker(udo);
        System.out.println("In polling, Data: " + udo.getData().getAsJsonObject().toString());
        String res = "pass";
        if (this.filterRuleMap.size() != 0) {
            if (!filteringUdo(udo)) {
                log.info("Udo not satisfying the filterRules, Reject update request!");
                res = "reject";
            }
        }

        eventBus.post(new SyncEvent(EventType.SYNC, udo, res.getBytes()));
    }

    //update udo by uri
    protected void updateUdoByUri(String tag, byte[] payload, byte[] uri, ContextInfo contextInfo,
        UriType uriType) {
        Udo udo = this.updateUdo(tag, payload);
        udo.setUri(new Uri(new String(uri), uriType));
        udo.setContextInfo(contextInfo);
        eventBus.post(new SaveByUriEvent(EventType.SAVE_BY_URI, udo, uri));
    }

    public Udo updateUdo(String id, byte[] payload) {
        JsonObject data = new Gson().fromJson(new String(payload), JsonObject.class);
        Udo udo = new Udo(null, data);
        udo.setId(id);
        return udo;
    }


}
