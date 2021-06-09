package info.nemoworks.udo.messaging.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRule {
    private String createBy;

    private String createTime;

    private String location;

    private String appId;

    public boolean isEqual(FilterRule filterRule){
        return filterRule.createBy.equals(this.createBy) && filterRule.createTime.equals(this.createBy)
                && filterRule.location.equals(this.location) && filterRule.appId.equals(this.appId);
    }
}
