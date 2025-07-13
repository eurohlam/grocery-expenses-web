package org.roag.groceryexpenses.data;

import lombok.Getter;

@Getter
public class ItemAggregator implements Comparable<ItemAggregator>{

    private final String category;
    private final String item;
    private int count = 0;
    private double total = 0;

    public ItemAggregator(String category, String item, double price) {
        this.category = category;
        this.item = item.toLowerCase().trim();
        addPrice(price);
    }

    public void addPrice(double price) {
        count++;
        total += price;
    }

    @Override
    public String toString() {
        return String.format("%s (x%d): $%.2f", item, count, total);
    }

    @Override
    public int compareTo(ItemAggregator another) {
        return this.item.compareTo(another.getItem());
    }
}

