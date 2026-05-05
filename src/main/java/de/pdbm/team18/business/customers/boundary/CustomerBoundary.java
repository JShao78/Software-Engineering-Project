package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerController;
import de.pdbm.team18.business.customers.repository.CustomerRepository;
import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.event.AjaxBehaviorEvent;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class CustomerBoundary implements Serializable {
	private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String state;
    private String zipCode;
    private String street;

    //Getters and Setters for properties
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }

    public String getEmail() { return email; }
    public void setEmail(String email) {this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    private int currentPage = 1;
    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;

    @Inject
    CustomerController customerController;
    @Inject
    CustomerRepository customerRepository;

    private Customer customer = new Customer();
    private Customer editCustomer = new Customer();

    public List<Customer> findPage() {
        return customerController.findPage(currentPage,pageSize);
    }

    public int totalPages() {
        return customerController.totalPages(pageSize);
    }

    public int count() {
        return customerController.count();
    }

    public void save() {
        customerController.create(customer);
        this.customer = new Customer();
    }

    public boolean prepareEdit(Integer customerId) {
        Customer foundCustomer = customerController.findById(customerId);
        if (foundCustomer == null) {
            cancelEdit();
            return false;
        }
        this.editCustomer = foundCustomer;
        return true;
    }

    public boolean saveEdit() {
        if (editCustomer.getCustomerId() == null) {
            return false;
        }

        boolean success = customerController.edit(editCustomer);
        if (success) {
            cancelEdit();
        }

        return success;
    }

    public void cancelEdit() {
        this.editCustomer = new Customer();
    }

    public void delete(Integer customerId) {
        customerController.delete(customerId);
    }

	public void addCustomer() {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setState(state);
        customer.setZipCode(zipCode);
        customer.setStreet(street);

        customerRepository.create(customer);

        this.firstName = null;
        this.lastName = null;
        this.email = null;
        this.phone = null;
        this.state = null;
        this.zipCode = null;
        this.street = null;
    }

    //Getter and Setter

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.clamp(currentPage, 1, Math.max(totalPages(), 1));
    }

    public int getPageSize() {
        return pageSize;
    }

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

    public void onPageSizeChange(AjaxBehaviorEvent event) {
        this.pageSize = Math.clamp(pageSize, 1, RepositoryQueryUtils.MAX_LIMIT);
        this.currentPage = 1;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getEditCustomer() {
        return editCustomer;
    }
}
