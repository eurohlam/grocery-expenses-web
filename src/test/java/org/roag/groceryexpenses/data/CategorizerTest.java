package org.roag.groceryexpenses.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategorizerTest {

    @Test
    void categorizeItem() {
        var categorizer = new Categorizer(Categorizer.CATEGORIES_JSON_FILE);
        Assertions.assertEquals("Meat & Seafood", categorizer.categorizeItem("SNAPPER FILLETS"));
        Assertions.assertNotEquals("Meat & Seafood", categorizer.categorizeItem("CUCUMBER"));
    }
}
