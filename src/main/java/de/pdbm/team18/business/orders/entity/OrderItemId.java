package de.pdbm.team18.business.orders.entity;

import java.util.Objects;
import java.io.Serializable;
public class OrderItemId implements Serializable {
    private Integer orderId;
    private Integer itemId;

    public OrderItemId(){}

    public OrderItemId(Integer orderId, Integer itemId){
        this.orderId = orderId;
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemId that = (OrderItemId) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, itemId);
    }


    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
}
