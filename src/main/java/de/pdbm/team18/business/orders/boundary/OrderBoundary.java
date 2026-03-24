package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.customers.boundary.CustomerBoundary;
import de.pdbm.team18.business.orders.entity.SalesOrder;
import de.pdbm.team18.business.products.boundary.ProductBoundary;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named
@RequestScoped
public class OrderBoundary {

    //TODO remove temporary sources for dummy data generation Customer and Product
    @Inject
    CustomerBoundary customerBoundary;
    @Inject
    ProductBoundary productBoundary;
    private ArrayList<SalesOrder> dummyData = new ArrayList<>(List.of(
            new SalesOrder(customerBoundary.getAllCustomers().getFirst())
    ));

    //TODO Replace with proper implementation
    public SalesOrder getOrder(UUID id) {
        for (SalesOrder so : dummyData) if (so.getId().equals(id)) return so;
        return null;
    }

    //TODO Replace with proper implementation
    public List<SalesOrder> getAllOrders() {
        return dummyData;
    }

    //TODO Replace with proper implementation
    public void addOrder(UUID customerID) {
        dummyData.add(new SalesOrder(customerBoundary.getCustomer(customerID)));
    }

}
