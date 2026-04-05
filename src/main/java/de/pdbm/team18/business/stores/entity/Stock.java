package de.pdbm.team18.business.stores.entity;

import de.pdbm.team18.business.products.entity.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "stocks")
public class Stock {

    @EmbeddedId
    private StockId stockId;

    @ManyToOne(optional = false)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    private Store store;

    @Column
    private int quantity;

    public StockId getStockId() {
        return stockId;
    }

    public void setStockId(StockId stockId) {
        this.stockId = stockId;
    }

    public Product getProduct() {
        return product;
    }

    public Store getStore() {
        return store;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
