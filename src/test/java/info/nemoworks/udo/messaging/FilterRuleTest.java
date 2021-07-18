package info.nemoworks.udo.messaging;

import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.gateway.MQTTGateway;
import info.nemoworks.udo.messaging.messaging.FilterRule;
import info.nemoworks.udo.model.Udo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;

public class FilterRuleTest {

    public String loadFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @Test
    public void filterRuleTest() throws IOException, MqttException {
        FilterRule filterRule = new FilterRule(loadFromFile("src/test/resources/testRules.json"));
        filterRule.printEqualValues();
        filterRule.printLargerThanValues();
        filterRule.printTimerLarger();
        filterRule.printTimerLess();
        Udo udo = new Udo(null);
        udo.setCreatedOn(0);
        udo.setCreatedBy("nemoworks");
        udo.setId("id");
        udo.setClock("12:00:00");
        JsonObject data = new JsonObject();
        data.addProperty("clock", "12:00:00");
        udo.setData(data);
        MQTTGateway mqttGateway = new MQTTGateway();
//        mqttGateway.addFilterRule("id", filterRule);
//        System.out.println(mqttGateway.filteringUdo(udo));
//        System.out.println(filterRule.filteringEqual(udo));
//        System.out.println(filterRule.filteringLarger(udo));
    }

    @Test
    public void testTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("HH:mm:ss");// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        System.out.println("现在时间：" + sdf.format(date)); // 输出已经格式化的现在时间（24小时制）
    }
}
