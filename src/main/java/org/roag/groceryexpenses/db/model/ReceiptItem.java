package org.roag.groceryexpenses.db.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "receipt_item")
public class ReceiptItem {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter @Setter
    @Column(nullable = false)
    private String item;

    @Getter @Setter
    @Column(nullable = false)
    private String price;

    @Override
    public String toString() {
        return String.format("%s: %s", getItem(), getPrice());
    }
}