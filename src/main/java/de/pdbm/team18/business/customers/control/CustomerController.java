package de.pdbm.team18.business.customers.control;

import de.pdbm.team18.business.customers.repository.CustomerRepository;
import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class CustomerController {

    @Inject
    private CustomerRepository customerRepository;

    public List<Customer> findPage(int pageNumber) {
        int offset = (pageNumber - 1) * RepositoryQueryUtils.DEFAULT_LIMIT;
        return customerRepository.findPage(offset, 0);
    }

    public int totalPages() {
        return Math.ceilDivExact(customerRepository.count(), RepositoryQueryUtils.DEFAULT_LIMIT);
    }

}
