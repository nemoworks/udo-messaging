package info.nemoworks.udo.messaging.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.Udo;
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
        JsonObject jsonObject = new Gson().fromJson(jsonStr, JsonObject.class);
        JsonArray equalValueArray = jsonObject.getAsJsonArray("equalValues");
        equalValueArray.forEach(
            eVal -> {
                eVal.getAsJsonObject().entrySet().forEach(
                    entry -> {
                        this.equalValues.put(entry.getKey(), entry.getValue());
                    }
                );
            }
        );
        JsonArray largerThanValueArray = jsonObject.getAsJsonArray("largerThanValues");
        largerThanValueArray.forEach(
            eVal -> {
                eVal.getAsJsonObject().entrySet().forEach(
                    entry -> {
                        this.largerThanValues.put(entry.getKey(), entry.getValue().getAsInt());
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

    public boolean filteringEqual(Udo udo) {
        this.result = true;
        equalValues.forEach(
            (key, value) -> {
                JsonObject udoJson = new Gson().fromJson(new Gson().toJson(udo), JsonObject.class);
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
                JsonObject udoJson = new Gson().fromJson(new Gson().toJson(udo), JsonObject.class);
                if (!udoJson.has(key)) {
                    this.result = false;
                } else if (!(udoJson.get(key).getAsInt() > this.largerThanValues
                    .get(key))) {
                    this.result = false;
                }
            }
        );
        return this.result;
    }
}
