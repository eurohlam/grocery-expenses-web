package org.roag.groceryexpenses.web;

import org.roag.groceryexpenses.data.Categorizer;
import org.roag.groceryexpenses.db.ReceiptItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    @Autowired
    private Categorizer categorizer;

    @GetMapping("/data")
    public String data(@RequestParam(name="name", required=false, defaultValue="Row Data") String name, Model model) {
        logger.info("Inside data controller");
        var items = receiptItemRepository.findAll();
        logger.info("Items {}", items);
        model.addAttribute("name", name);
        model.addAttribute("items", items);
        return "data";
    }

    @GetMapping("/categories")
    public String categories(@RequestParam(name="name", required=false, defaultValue="Categorized Data") String name, Model model) {
        logger.info("Inside categories controller");
        var items = receiptItemRepository.findAll();
        var categorized = categorizer.categorizeItems(items);
        logger.info("Categorized {}", categorized);
        model.addAttribute("name", name);
        model.addAttribute("categorized", categorized);
        return "categories";
    }

}
