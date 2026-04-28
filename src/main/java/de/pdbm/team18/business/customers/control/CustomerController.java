package de.pdbm.team18.business.customers.control;

import de.pdbm.team18.business.customers.repository.CustomerRepository;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class CustomerController {

    @Inject
    private CustomerRepository customerRepository;

    public List<Customer> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return customerRepository.findPage(offset, pageSize);
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

    public Customer findById(Integer customerId) {
        return customerRepository.findById(customerId);
    }

    public boolean edit(Customer customer) {
        return customerRepository.update(customer) != null;
    }
    
    public boolean delete(Integer customerId) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            return false;
        }
        if (customerRepository.hasOrders(customerId)) {
            return false;
        }
        customerRepository.delete(customerId);
        return true;
    }
}
