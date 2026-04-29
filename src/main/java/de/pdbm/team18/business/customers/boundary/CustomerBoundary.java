package de.pdbm.team18.business.customers.boundary;

import de.pdbm.team18.business.customers.control.CustomerController;
import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
        try{
            customerController.delete(customerId);
            addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Kunde erfolgreich gelöscht");
        } catch (IllegalArgumentException e){
            addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
        } catch (IllegalStateException e){
            addMessage(FacesMessage.SEVERITY_WARN, "Warnung", e.getMessage());
        }
    }
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
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
