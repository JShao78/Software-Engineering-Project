package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerController;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
public class CustomerBoundary {

    @Inject
    CustomerController customerController;

    public List<Customer> findPage(int offset, int limit) {
        return customerController.findPage(offset, limit);
    }

    @Deprecated
    public List<Customer> allCustomers() {
        return customerController.findAll();
    }

}
