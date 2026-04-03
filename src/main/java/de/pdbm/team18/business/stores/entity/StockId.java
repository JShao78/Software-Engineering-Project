package de.pdbm.team18.business.stores.entity;

import java.util.Objects;

public class StockId {

    private int storeId;

    private int productId;

    //Default constructor
    public StockId() {

    }

    public StockId(int storeId, int productId) {
        this.storeId = storeId;
        this.productId = productId;
    }

    public int getStoreId() {
        return storeId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StockId other = (StockId) obj;
        return this.storeId == other.storeId
                && this.productId == other.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.storeId) * Objects.hashCode(this.productId);
    }
}
