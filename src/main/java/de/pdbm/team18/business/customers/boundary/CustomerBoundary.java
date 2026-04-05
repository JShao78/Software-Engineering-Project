package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerController;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
public class CustomerBoundary {

    //TODO implement property values into View
    private int offset;
    private int limit;

    @Inject
    CustomerController customerController;

    public List<Customer> findPage() {
        return customerController.findPage(offset, limit);
    }

    @Deprecated
    public List<Customer> allCustomers() {
        return customerController.findAll();
    }

    //Getter and Setter


    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
