package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerControl;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.UUID;

@Named
@RequestScoped
public class CustomerBoundary {
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String phone;

    //Getters and Setters for properties
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) {this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Inject
    private CustomerControl controller;

    /**
     * Calls the CustomerController to return the Customer with the provided UUID.
     * @param id the UUID of the Customer entity.
     * @return the Customer entity with the provided UUID or null if none exists.
     */
    public Customer getCustomer(UUID id) {
        return controller.findCustomerById(id);
    }

    /**
     * Returns the selected page of Customers matching the search term. See repository for more info.
     * @param searchTerm the search term to match the Customers.
     * @param offset the page number.
     * @param limit the number of Customers per page.
     * @return a list representing the page.
     */
    public List<Customer> getAllCustomers(String searchTerm, int offset, int limit) {
        return controller.findPage(searchTerm, offset, limit);
    }

    /**
     * Calls proper getAllCustomers()-method with default parameters.
     * @deprecated Use parameterized method instead. TODO remove once unused.
     * @return a list of up to 100 Customers.
     */
    public List<Customer> getAllCustomers() {
        return getAllCustomers(null, 0, 100);
    }

    /**
     * Creates a Customer entity and sends it to the controller to be stored persistently.
     */
    public void addCustomer() {
        Customer customer = new Customer(firstName, lastName);
        customer.setAddress(address);
        customer.setEmail(email);
        customer.setPhone(phone);
        controller.saveCustomer(customer);
        this.firstName = null;
        this.lastName = null;
        this.address = null;
        this.email = null;
        this.phone = null;
    }

}
