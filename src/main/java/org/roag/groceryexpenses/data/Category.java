package org.roag.groceryexpenses.data;

import java.util.List;
import java.util.Objects;

public record Category(String name, List<String> keywords) {

    public Category {
        Objects.requireNonNull(name);
        Objects.requireNonNull(keywords);
    }
}
