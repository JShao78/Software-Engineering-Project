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

    public List<Integer> totalPages() {
        List<Integer> pages = new java.util.ArrayList<>();
		for (int i = 1; i <= customerController.totalPages(); i++) {
			pages.add(i);
		}
		return pages;
    }

    //Getter and Setter

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
