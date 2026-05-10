package de.pdbm.team18.business.customers.control;

import de.pdbm.team18.business.customers.repository.CustomerRepository;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;


@Stateless
@RolesAllowed({"ADMIN", "USER1", "USER2"})
public class CustomerController {

    @Inject
    private CustomerRepository customerRepository;

    public List<Customer> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return customerRepository.findPage(offset, pageSize);
    }

    public List<Customer> findByName(String name) {
        return customerRepository.findByName(name);
    }

    public Customer findById(int id) {
        return customerRepository.findById(id);
    }

    public int totalPages(int pageSize) {
        return Math.ceilDivExact(customerRepository.count(), pageSize);
    }

    public int count() {
        return customerRepository.count();
    }

    public void create(Customer customer) {
        customerRepository.create(customer);
    }

    @RolesAllowed({"ADMIN", "USER1"})
    public boolean edit(Customer customer) {
        return customerRepository.update(customer) != null;
    }

    @RolesAllowed({"ADMIN", "USER1"})
    public void delete(Integer customerId) {
        Customer customer = customerRepository.findById(customerId);
        if(customer == null){
            throw new IllegalArgumentException("Kunde wurde nicht gefunden");
        }
        if(customerRepository.hasOrders(customerId)){
            throw new IllegalStateException("Kunde kann aufgrund bestehender Bestellungen nicht gelöscht werden.");
        }
        customerRepository.delete(customerId);
    }

}
