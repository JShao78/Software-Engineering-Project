package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerController;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class CustomerBoundary implements Serializable {

    private int currentPage = 1;

    @Inject
    CustomerController customerController;

    public List<Customer> findPage() {
        return customerController.findPage(currentPage);
    }

    public int totalPages() {
        return customerController.totalPages();
    }

    //Getter and Setter

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.clamp(currentPage, 1, Math.max(totalPages(), 1));
    }
}
