package org.roag.groceryexpenses.data;

import com.opencsv.CSVReader;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.roag.groceryexpenses.db.model.ReceiptItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.util.*;

@Component
public class Categorizer {

    private static final Logger logger = LoggerFactory.getLogger(Categorizer.class);

    public static final String CATEGORIES_JSON_FILE = "src/main/resources/categories.json";

    @Getter
    private List<Category> categories;

    public Categorizer(@Value("${categorizer.json}") String jsonFile) {
        this.categories = loadCategories(jsonFile);
    }

    public List<Category> loadCategories(String jsonFile) {
        logger.info("Loading categories from file {}", jsonFile);
        JSONParser parser = new JSONParser();
        List<Category> categories = new ArrayList<>();

        try (FileReader reader = new FileReader(jsonFile)) {
            // Parse JSON file
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            jsonObject.keySet().forEach(key -> {
                List<String> keywords = (List<String>)jsonObject.get(key);
                var category = new Category(key.toString(), keywords);
                categories.add(category);
            });

        } catch (Exception e) {
            logger.error("Failed to load categories from file {}", jsonFile, e);
        }
        return categories;
    }

    public String categorizeItem(String item) {
        logger.debug("Categorizing {}", item);
        var lowerItem = item.toLowerCase();
        var categories = getCategories();
        var matchedCategory =  categories.stream()
                .filter(category -> category.keywords().stream().anyMatch(lowerItem::contains))
                .map(Category::name)
                .findFirst()
                .orElse("Other");
        logger.debug("Matched category {} for {}", matchedCategory, item);
        return matchedCategory;
    }

    public Map<String, Set<ItemAggregator>> categorizeReceiptItems(List<ReceiptItem> receiptItems) {
        Map<String, Set<ItemAggregator>> categorized = new HashMap<>();
        for (ReceiptItem receiptItem : receiptItems) {
            var category = categorizeItem(receiptItem.getItem());
            var items = new TreeSet<ItemAggregator>();
            items.add(new ItemAggregator(category, receiptItem.getItem(), Double.parseDouble(receiptItem.getPrice())));
            categorized.putIfAbsent(category, items);
            categorized
                    .get(category)
                    .stream()
                    .filter(item->item.getItem().equals(receiptItem.getItem().toLowerCase().trim()))
                    .findFirst()
                    .ifPresentOrElse(item->item.addPrice(Double.parseDouble(receiptItem.getPrice())),
                            () -> categorized.get(category).add(new ItemAggregator(category, receiptItem.getItem(), Double.parseDouble(receiptItem.getPrice()))));

        }
        return categorized;
    }

    public static void main(String[] args) {
        String csvFile = "src/main/resources/receipt_item.csv";
        var categorizer = new Categorizer("src/main/resources/categories.json");

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> lines = reader.readAll();
            int itemIdx = Arrays.asList(lines.get(0)).indexOf("item");
            int priceIdx = Arrays.asList(lines.get(0)).indexOf("price");

            Map<String, Map<String, ItemAggregator>> categorized = new HashMap<>();

            for (int i = 1; i < lines.size(); i++) {
                String item = lines.get(i)[itemIdx].trim();
                double price = Double.parseDouble(lines.get(i)[priceIdx]);
                String category = categorizer.categorizeItem(item);

                categorized.putIfAbsent(category, new HashMap<>());
                Map<String, ItemAggregator> items = categorized.get(category);
                items.putIfAbsent(item, new ItemAggregator(category, item, price));
                //items.get(item).addPrice(price);
            }

            // Output
            for (String category : categorized.keySet()) {
                System.out.println("\nCategory: " + category);
                for (ItemAggregator itemData : categorized.get(category).values()) {
                    System.out.println("  " + itemData);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
