package de.pdbm.team18.business.customers.control;

import de.pdbm.team18.business.customers.repository.CustomerRepository;
import jakarta.ejb.Stateless;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@Stateless
public class CustomerControl {
    @Inject
    private CustomerRepository customerRepository;

    public List<Customer> findPage(String searchTerm, int offset, int limit) {
        return customerRepository.findPage(searchTerm, offset, limit);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public long countCustomers(String searchTerm) {
        return customerRepository.count(searchTerm);
    }

    public Customer findCustomerById(UUID id) {
        return customerRepository.findById(id).orElse(null);
    }


}
