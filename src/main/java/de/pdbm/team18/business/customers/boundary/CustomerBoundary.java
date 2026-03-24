package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerControl;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named
@RequestScoped
public class CustomerBoundary {

    //TODO remove after implementation
    private ArrayList<Customer> dummyData = new ArrayList<>(List.of(
            new Customer("John", "Doe"),
            new Customer("Max", "Mustermann")
    ));

    @Inject
    CustomerControl controller;

    //TODO Replace with proper implementation
    public Customer getCustomer(UUID id) {
        for (Customer c : dummyData) if (c.getId().equals(id)) return c;
        return null;
    }

    //TODO Replace with proper implementation
    public List<Customer> getAllCustomers() {
        return dummyData;
    }

    //TODO Replace with proper implementation
    public void addCustomer(String firstName, String lastName) {
        dummyData.add(new Customer(firstName, lastName));
    }

}
