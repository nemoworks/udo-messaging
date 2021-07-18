package info.nemoworks.udo.messaging.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import info.nemoworks.udo.model.DistanceUtil;
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

    private String filterString;

    private Map<String, Integer> largerThanValues;

    private Map<String, Integer> lessThanValues;

    private Map<String, String> timerLarger;

    private Map<String, String> timerLess;

    private Map<String, Float> distanceScope;

    public FilterRule(String jsonStr) {
        this.equalValues = new HashMap<>();
        this.largerThanValues = new HashMap<>();
        this.lessThanValues = new HashMap<>();
        this.timerLarger = new HashMap<>();
        this.timerLess = new HashMap<>();
        this.distanceScope = new HashMap<>();
        this.filterString = jsonStr;
        JsonObject jsonObject = new Gson().fromJson(jsonStr, JsonObject.class);
        if (jsonObject.has("equalValues")) {
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
        }
        if (jsonObject.has("largerThanValues")) {
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
        if (jsonObject.has("lessThanValues")) {
            JsonArray lessThanValueArray = jsonObject.getAsJsonArray("lessThanValues");
            lessThanValueArray.forEach(
                eVal -> {
                    eVal.getAsJsonObject().entrySet().forEach(
                        entry -> {
                            this.lessThanValues.put(entry.getKey(), entry.getValue().getAsInt());
                        }
                    );
                }
            );
        }
        if (jsonObject.has("timeLargerValues")) {
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
        }
        if (jsonObject.has("timeLessValues")) {
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
        if (jsonObject.has("distanceScopes")) {
            JsonArray distanceScopeArray = jsonObject.getAsJsonArray("distanceScopes");
            distanceScopeArray.forEach(
                eVal -> {
                    eVal.getAsJsonObject().entrySet().forEach(
                        entry -> {
                            this.distanceScope.put(entry.getKey(), entry.getValue().getAsFloat());
                        }
                    );
                }
            );
        }
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

    public void printDistanceScope() {
        distanceScope.forEach(
            (key, value) -> {
                System.out.println(key + ": " + value);
            }
        );
    }

    public boolean filteringEqual(Udo udo) {
        this.result = true;
        if (equalValues.size() == 0) {
            return true;
        }
        equalValues.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (udoJson.has(key)) {
                    if (!udoJson.get(key).equals(this.equalValues.get(key))) {
                        this.result = false;
                    }
                }
            }
        );
        return this.result;
    }

    public boolean filteringLarger(Udo udo) {
        this.result = true;
        if (largerThanValues.size() == 0) {
            return true;
        }
        largerThanValues.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (udoJson.has(key)) {
                    if (!(udoJson.get(key).getAsInt() > this.largerThanValues
                        .get(key))) {
                        this.result = false;
                    }
                }
            }
        );
        return this.result;
    }

    public boolean filteringLess(Udo udo) {
        this.result = true;
        if (lessThanValues.size() == 0) {
            return true;
        }
        lessThanValues.forEach(
            (key, value) -> {
                JsonObject udoJson = (JsonObject) udo.getData();
                if (udoJson.has(key)) {
                    if (!(udoJson.get(key).getAsInt() <= this.lessThanValues
                        .get(key))) {
                        this.result = false;
                    }
                }
            }
        );
        return this.result;
    }

    public boolean filteringTimerLess(Udo udo) {
        this.result = true;
        if (timerLess.size() == 0) {
            return true;
        }
        timerLess.forEach(
            (key, value) -> {
                JsonObject udoJson = new Gson().fromJson(new Gson().toJson(udo), JsonObject.class);
                if (udoJson.has(key)) {
                    String uTime = udoJson.get(key).getAsString();
                    String benchTime = this.timerLess.get(key);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    try {
                        Date uDate = sdf.parse(uTime);
//                    Date uDate = new Date();
                        Date benchDate = sdf.parse(benchTime);
                        System.out.println("Check Clock: " + sdf.format(uDate)
                            + " should be before: " + sdf.format(benchDate));
                        if (!uDate.before(benchDate)) {
                            this.result = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        );

        return this.result;

    }

    public boolean filteringTimerLarger(Udo udo) {
        this.result = true;
        if (timerLarger.size() == 0) {
            return true;
        }
        timerLarger.forEach(
            (key, value) -> {
                JsonObject udoJson = new Gson().fromJson(new Gson().toJson(udo), JsonObject.class);
//                System.out.println(udoJson.toString());
//                System.out.println(key);
                if (udoJson.has(key)) {
                    System.out.println("in judge...");
                    String uTime = udoJson.get(key).getAsString();
                    String benchTime = this.timerLarger.get(key);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    try {
                        Date uDate = sdf.parse(uTime);
                        Date benchDate = sdf.parse(benchTime);
                        System.out.println("Check Clock: " + sdf.format(uDate)
                            + " should be after: " + sdf.format(benchDate));
                        if (!uDate.after(benchDate)) {
                            this.result = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
        return this.result;
    }

    public boolean filteringAll(Udo udo) {
        System.out.println("filtering all... id: " + udo.getId());
        return filteringTimerLarger(udo) && filteringTimerLess(udo)
            && filteringLarger(udo) && filteringEqual(udo)
            && filteringLess(udo);
    }

    public boolean filteringDistance(Udo udo1, Udo udo2) {
        this.result = true;
        if (this.distanceScope.size() == 0) {
            return true;
        }
        distanceScope.forEach(
            (key, value) -> {
                System.out.println("Check Distance Between " + udo1.getId() + " and "
                    + udo2.getId() + " in scope: " + value + " m...");
                float latitude1 = udo1.getData().getAsJsonObject().get("location")
                    .getAsJsonObject().get("latitude").getAsFloat();
                float longitude1 = udo1.getData().getAsJsonObject().get("location")
                    .getAsJsonObject().get("longitude").getAsFloat();
                float latitude2 = udo2.getData().getAsJsonObject().get("location")
                    .getAsJsonObject().get("latitude").getAsFloat();
                float longitude2 = udo2.getData().getAsJsonObject().get("location")
                    .getAsJsonObject().get("longitude").getAsFloat();
                float distance = DistanceUtil.getDistance(
                    longitude1, latitude1, longitude2, latitude2
                );
                System.out.println("Real Distance: " + distance);
                System.out.println("Scope Set: " + value);
                if (distance > (float) value) {
                    System.out.println("Distance Out of Scope..");
                    this.result = false;
                }
            }
        );
        return this.result;
    }
}
