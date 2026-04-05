package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.orders.control.OrderController;
import de.pdbm.team18.business.orders.entity.Order;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class OrderBoundary {
    @Inject
    OrderController orderController;


    public List<Order> getAllOrders() {

        return orderController.findPage(0, 10000);
    }
}
