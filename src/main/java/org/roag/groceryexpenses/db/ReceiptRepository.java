package org.roag.groceryexpenses.db;

import org.roag.groceryexpenses.db.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}