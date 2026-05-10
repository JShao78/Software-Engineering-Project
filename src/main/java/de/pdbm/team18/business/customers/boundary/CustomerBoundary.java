package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerController;
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

    private int currentPage = 1;
    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;

    @Inject
    CustomerController customerController;

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

    public void create() {
        customerController.create(customer);
        this.customer = new Customer();
    }

    public boolean prepareEdit(Customer customerToEdit) {
        if (customerToEdit == null || customerToEdit.getCustomerId() == null) {
            cancelEdit();
            return false;
        }

        this.editCustomer = customerToEdit;
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
