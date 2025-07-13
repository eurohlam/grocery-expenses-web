package org.roag.groceryexpenses.db;

import org.roag.groceryexpenses.db.model.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {
}