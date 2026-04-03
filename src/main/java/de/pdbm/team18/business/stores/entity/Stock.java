package de.pdbm.team18.business.stores.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stocks")
@IdClass(StockId.class)
public class Stock {

    @Id
    @JoinColumn(name = "product_id")
    private int productId;

    @Id
    @JoinColumn(name = "store_id")
    private int storeId;

    @Column
    private int quantity;

    public int getProductId() {
        return productId;
    }

    public int getStoreId() {
        return storeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
