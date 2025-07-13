package org.roag.groceryexpenses.rest;

import jakarta.transaction.Transactional;
import org.roag.groceryexpenses.db.ReceiptRepository;
import org.roag.groceryexpenses.db.model.Receipt;
import org.roag.groceryexpenses.db.model.ReceiptItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

    @Autowired
    private ReceiptRepository receiptRepository;

    @Transactional
    public void addReceipt(Receipt receipt) throws NoSuchElementException, IllegalArgumentException {
        //removing meta items
        var filteredItems = new ArrayList<ReceiptItem>();
        for (ReceiptItem item : receipt.getItems()) {
            if (!(item.getItem().contains("TOTAL") || item.getItem().contains("GST")
                || item.getItem().contains("CHANGE") || item.getItem().contains("EFTPOS")
                || item.getItem().contains("BALANCE") )) {
                filteredItems.add(item);
            }
        }
        receipt.setItems(filteredItems);
        logger.info("Adding new receipt after data validation {}", receipt);
        receiptRepository.save(receipt);
    }

}
