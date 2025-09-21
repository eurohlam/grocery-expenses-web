package org.roag.groceryexpenses.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategorizerTest {

    @Test
    void categorizeItemByKeywordMatching() {
        var categorizer = new Categorizer(Categorizer.CATEGORIES_JSON_FILE);
        Assertions.assertEquals("Meat & Seafood", categorizer.categorizeItemByKeywordMatching("SNAPPER FILLETS"));
        Assertions.assertNotEquals("Meat & Seafood", categorizer.categorizeItemByKeywordMatching("CUCUMBER"));
    }
}
