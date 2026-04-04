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

    public List<Customer> findPage(int offset, int limit) {
        return customerRepository.findPage(offset, limit);
    }

    @Deprecated
    public List<Customer> findAll() {
        return findPage(0, (int) count());
    }

    public long count() {
        return customerRepository.count();
    }

}
