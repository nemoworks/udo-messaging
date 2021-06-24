package info.nemoworks.udo.messaging;

import info.nemoworks.udo.messaging.messaging.FilterRule;
import info.nemoworks.udo.model.Udo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class FilterRuleTest {

    public String loadFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @Test
    public void filterRuleTest() throws IOException {
        FilterRule filterRule = new FilterRule(loadFromFile("src/test/resources/testRules.json"));
        filterRule.printEqualValues();
        filterRule.printLargerThanValues();
        Udo udo = new Udo(null);
        udo.setCreatedOn(0);
        udo.setCreatedBy("nemoworks");
        System.out.println(filterRule.filteringEqual(udo));
        System.out.println(filterRule.filteringLarger(udo));
    }
}
