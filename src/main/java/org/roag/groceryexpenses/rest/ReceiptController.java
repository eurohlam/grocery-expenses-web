package org.roag.groceryexpenses.rest;

import org.roag.groceryexpenses.db.model.Receipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);

    @Autowired
    private ReceiptService receiptService;

    @PostMapping("/")
    public ResponseEntity<?> addReceipt(@RequestBody Receipt receipt) {
        try {
            logger.info("Called addReceipt API with data: {}", receipt);
            receiptService.addReceipt(receipt);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getErrorJson("NOT_FOUND",
                            e.getMessage(),
                            "/receipt/"));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getErrorJson("BAD_REQUEST",
                            e.getMessage(),
                            "/receipt/"));
        }
    }

    private String getErrorJson(String errorStatus, String errorMessage, String path) {
        return """
                {
                   "status": "%s",
                   "errorMessage": "%s",
                   "path": "%s"
                }
                """
                .formatted(errorStatus, errorMessage, path);
    }
}
