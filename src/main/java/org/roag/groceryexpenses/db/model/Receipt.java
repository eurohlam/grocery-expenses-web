package org.roag.groceryexpenses.db.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "receipt")
public class Receipt {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter @Setter
    @Column(nullable = false)
    private String storeName;

    @Getter @Setter
    @Column(nullable = false)
    private Date transactionDate = new Date();

    @Getter @Setter
    @OneToMany(targetEntity = ReceiptItem.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "receipt_id", nullable = false)
    private List<ReceiptItem> items = new ArrayList<>();

    @Override
    public String toString() {
        return String.format("storeName: %s, transactionDate: %s, items: %s", getStoreName(), getTransactionDate(), getItems());
    }
}
