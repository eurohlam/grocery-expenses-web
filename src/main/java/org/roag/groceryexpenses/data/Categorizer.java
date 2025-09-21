package org.roag.groceryexpenses.data;

import com.opencsv.CSVReader;
import lombok.Getter;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
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

    private static final String DEFAULT_CATEGORY_NAME = "Other";

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

    public String categorizeItemByKeywordMatching(String item) {
        logger.debug("Categorizing {}", item);
        var lowerItem = item.toLowerCase().trim();
        var categories = getCategories();
        var matchedCategory =  categories.stream()
                .filter(category -> category.keywords().stream().anyMatch(lowerItem::contains)) //TODO: it works bad for short keywords like bun or tea.
                .map(Category::name)
                .findFirst()
                .orElse(DEFAULT_CATEGORY_NAME);
        logger.debug("Matched category {} for {}", matchedCategory, item);
        return matchedCategory;
    }

    public Map<String, Set<ItemAggregator>> categorizeItems(List<ReceiptItem> items) {
        var ctgMap = new HashMap<String, Set<ReceiptItem>>();
        // phase 1: keyword matching
        logger.info("Phase 1: Categorizing by keyword matching");
        for (ReceiptItem receiptItem : items) {
            var item = receiptItem.getItem().trim();
            var category = categorizeItemByKeywordMatching(item);
            if (ctgMap.containsKey(category)) {
                ctgMap.get(category).add(receiptItem);
            } else {
                var  set = new HashSet<ReceiptItem>();
                set.add(receiptItem);
                ctgMap.put(category, set);
            }
        }
        // phase 2: fuzzy logic for uncategorized items looking up similarities among categorized items
        logger.info("Phase 2: Categorizing with fuzzy logic looking up similarities among categorized items");
        var itemsToRemove = new HashSet<ReceiptItem>();
        var others = ctgMap.get(DEFAULT_CATEGORY_NAME);
        JaroWinklerSimilarity jws = new JaroWinklerSimilarity();

        for (ReceiptItem receiptItem : others) {
            double similarity = 0;
            String newCategory = DEFAULT_CATEGORY_NAME;
            String similarItem = "none";

            var item = receiptItem.getItem().toLowerCase().trim();
            logger.debug("Looking up a new category for item [ {} ]", item);
            for (var category: ctgMap.keySet()) {
                if (!DEFAULT_CATEGORY_NAME.equals(category)) {
                    for (var sortedItem: ctgMap.get(category)) {
                        var sItem = sortedItem.getItem().toLowerCase().trim();
                        Double currentSimilarity = jws.apply(item, sItem);

                        if (currentSimilarity > similarity) {
                            similarity = currentSimilarity;
                            newCategory = category;
                            similarItem = sItem;
                        }
                    }
                }
            }
            logger.debug("Item [ {} ] got max similarity [ {} ] with item [ {} ] from category [ {} ]", item, similarity, similarItem, newCategory);
            if (similarity > 0.9) {
                logger.info("Item [ {} ] got max similarity [ {} ] with item [ {} ] from category [ {} ]", item, similarity, similarItem, newCategory);
                logger.info("Moving item [ {} ] from category [ {} ] to category [ {} ]", item, DEFAULT_CATEGORY_NAME, newCategory);
                ctgMap.get(newCategory).add(receiptItem);
                itemsToRemove.add(receiptItem);
            }
        }
        // removing categorized elements from default category
        for (ReceiptItem receiptItem : itemsToRemove) {
            ctgMap.get(DEFAULT_CATEGORY_NAME).remove(receiptItem);
        }
        // phase 3: fuzzy logic for uncategorized item by keyword matching
        logger.info("Phase 3: Categorizing with fuzzy logic for uncategorized item by keyword matching");
        itemsToRemove = new HashSet<>();
        var categories = getCategories();

        for (ReceiptItem receiptItem : others) {
            double similarity = 0;
            String newCategory = DEFAULT_CATEGORY_NAME;
            String similarItem = "none";
            var item = receiptItem.getItem().toLowerCase().trim();
            logger.debug("Looking up a new category for item [ {} ]", item);
            for (var token: item.split("\s")) {
                for (var category: categories) {
                    for (var keyword: category.keywords()) {
                        Double currentSimilarity = jws.apply(token.toLowerCase().trim(), keyword);

                        if (currentSimilarity > similarity) {
                            similarity = currentSimilarity;
                            newCategory = category.name();
                            similarItem = keyword;
                        }
                    }
                }
            }
            logger.debug("Item [ {} ] got max similarity [ {} ] with item [ {} ] from category [ {} ]", item, similarity, similarItem, newCategory);
            if (similarity > 0.9) {
                logger.info("Item [ {} ] got max similarity [ {} ] with item [ {} ] from category [ {} ]", item, similarity, similarItem, newCategory);
                logger.info("Moving item [ {} ] from category [ {} ] to category [ {} ]", item, DEFAULT_CATEGORY_NAME, newCategory);
                ctgMap.get(newCategory).add(receiptItem);
                itemsToRemove.add(receiptItem);
            }
        }
        // removing categorized elements from default category
        for (ReceiptItem receiptItem : itemsToRemove) {
            ctgMap.get(DEFAULT_CATEGORY_NAME).remove(receiptItem);
        }

        logger.info("End of categorizing");

        Map<String, Set<ItemAggregator>> categorized = new HashMap<>();
        for (var entry : ctgMap.entrySet()) {
            var itemAggregators = new TreeSet<ItemAggregator>();
            categorized.putIfAbsent(entry.getKey(), itemAggregators);
            for (var receiptItem : entry.getValue()) {
                itemAggregators.stream()
                        .filter(ia -> ia.getItem().equals(receiptItem.getItem().toLowerCase().trim()))
                        .findFirst()
                        .ifPresentOrElse(ia -> ia.addPrice(Double.parseDouble(receiptItem.getPrice())),
                                () -> itemAggregators.add(
                                        new ItemAggregator(entry.getKey(), receiptItem.getItem().toLowerCase().trim(),
                                                Double.parseDouble(receiptItem.getPrice()))));
            }
        }

        return categorized;
    }

    public Map<String, Set<ItemAggregator>> categorizeReceiptItems(List<ReceiptItem> receiptItems) {
        Map<String, Set<ItemAggregator>> categorized = new HashMap<>();
        for (ReceiptItem receiptItem : receiptItems) {
            var category = categorizeItemByKeywordMatching(receiptItem.getItem());
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

            var items = new ArrayList<ReceiptItem>();
            for (String[] line : lines) {
                ReceiptItem receiptItem = new ReceiptItem();
                receiptItem.setItem(line[itemIdx]);
                receiptItem.setPrice(line[priceIdx]);
                items.add(receiptItem);
            }

            logger.info("============= New Categorizing ===============");
            var ctgMap = categorizer.categorizeItems(items);
            // output
            ctgMap.forEach((category, set) -> {
                System.out.println(category + ":");
                set.forEach(System.out::println);
                System.out.println();
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
