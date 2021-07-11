package info.nemoworks.udo.messaging.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.Udo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRule {

    private Map<String, Object> equalValues;

    private boolean result;


    //    private Map<String, Object> notEqualValues;
//
    private Map<String, Integer> largerThanValues;

    private Map<String, Integer> lessThanValues;

    private Map<String, String> timerLarger;

    private Map<String, String> timerLess;

    //
//    private Map<String, Integer> LessThanValues;
//
//    private Map<String, String> pairValues;
//    private String createBy;
//
//    private String createTime;
//
//    private String location;
//
//    private String appId;
//
//    public boolean isEqual(FilterRule filterRule){
//        return filterRule.createBy.equals(this.createBy) && filterRule.createTime.equals(this.createBy)
//                && filterRule.location.equals(this.location) && filterRule.appId.equals(this.appId);
//    }
    public FilterRule(String jsonStr) {
        this.equalValues = new HashMap<>();
        this.largerThanValues = new HashMap<>();
        this.lessThanValues = new HashMap<>();
        this.timerLarger = new HashMap<>();
        this.timerLess = new HashMap<>();
        JsonObject jsonObject = new Gson().fromJson(jsonStr, JsonObject.class);
//        JsonArray equalValueArray = jsonObject.getAsJsonArray("equalValues");
//        equalValueArray.forEach(
//            eVal -> {
//                eVal.getAsJsonObject().entrySet().forEach(
//                    entry -> {
//                        this.equalValues.put(entry.getKey(), entry.getValue());
//                    }
//                );
//            }
//        );
//        JsonArray largerThanValueArray = jsonObject.getAsJsonArray("largerThanValues");
//        largerThanValueArray.forEach(
//            eVal -> {
//                eVal.getAsJsonObject().entrySet().forEach(
//                    entry -> {
//                        this.largerThanValues.put(entry.getKey(), entry.getValue().getAsInt());
//                    }
//                );
//            }
//        );
//        JsonArray lessThanValueArray = jsonObject.getAsJsonArray("lessThanValues");
//        largerThanValueArray.forEach(
//            eVal -> {
//                eVal.getAsJsonObject().entrySet().forEach(
//                    entry -> {
//                        this.lessThanValues.put(entry.getKey(), entry.getValue().getAsInt());
//                    }
//                );
//            }
//        );
        JsonArray timerLargerValueArray = jsonObject.getAsJsonArray("timeLargerValues");
        timerLargerValueArray.forEach(
            eVal -> {
                eVal.getAsJsonObject().entrySet().forEach(
                    entry -> {
                        this.timerLarger.put(entry.getKey(), entry.getValue().getAsString());
                    }
                );
            }
        );
        JsonArray timerLessValueArray = jsonObject.getAsJsonArray("timeLessValues");
        timerLessValueArray.forEach(
            eVal -> {
                eVal.getAsJsonObject().entrySet().forEach(
                    entry -> {
                        this.timerLess.put(entry.getKey(), entry.getValue().getAsString());
                    }
                );
            }
        );
    }

    public void printEqualValues() {
        equalValues.forEach(
            (key, value) -> {
                System.out.println(key + ": " + value);
            }
        );
    }

    public void printLargerThanValues() {
        largerThanValues.forEach(
            (key, value) -> {
                System.out.println(key + ": " + value);
            }
        );
    }

    public void printTimerLarger() {
        timerLarger.forEach(
            (key, value) -> {
                System.out.println(key + ": " + value);
            }
        );
    }

    public void printTimerLess() {
        timerLess.forEach(
            (key, value) -> {
                System.out.println(key + ": " + value);
            }
        );
    }

    public boolean filteringEqual(Udo udo) {
        this.result = true;
        equalValues.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (!udoJson.has(key)) {
                    this.result = false;
                } else if (!udoJson.get(key).equals(this.equalValues.get(key))) {
                    this.result = false;
                }
            }
        );
        return this.result;
    }

    public boolean filteringLarger(Udo udo) {
        this.result = true;
        largerThanValues.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (!udoJson.has(key)) {
                    this.result = false;
                } else if (!(udoJson.get(key).getAsInt() >= this.largerThanValues
                    .get(key))) {
                    this.result = false;
                }
            }
        );
        return this.result;
    }

    public boolean filteringLess(Udo udo) {
        this.result = true;
        lessThanValues.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (!udoJson.has(key)) {
                    this.result = false;
                } else if (!(udoJson.get(key).getAsInt() <= this.lessThanValues
                    .get(key))) {
                    this.result = false;
                }
            }
        );
        return this.result;
    }

    public boolean filteringTimerLess(Udo udo) {
        this.result = true;
        timerLess.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (!udoJson.has(key)) {
                    this.result = false;
                } else {
                    String uTime = udoJson.get(key).getAsString();
                    String benchTime = this.timerLarger.get(key);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    try {
                        Date uDate = sdf.parse(uTime);
                        Date benchDate = sdf.parse(benchTime);
                        if (!uDate.before(benchDate)) {
                            this.result = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
//                    if (!(udoJson.get(key).getAsInt() <= this.largerThanValues
//                        .get(key))) {
//                        this.result = false;
//                    }
                }
            }
        );
        return this.result;
    }

    public boolean filteringTimerLarger(Udo udo) {
        this.result = true;
        timerLarger.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (!udoJson.has(key)) {
                    this.result = false;
                } else {
                    String uTime = udoJson.get(key).getAsString();
                    String benchTime = this.timerLarger.get(key);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    try {
                        Date uDate = sdf.parse(uTime);
                        Date benchDate = sdf.parse(benchTime);
                        if (!uDate.after(benchDate)) {
                            this.result = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
//                    if (!(udoJson.get(key).getAsInt() <= this.largerThanValues
//                        .get(key))) {
//                        this.result = false;
//                    }
                }
            }
        );
        return this.result;
    }
}
