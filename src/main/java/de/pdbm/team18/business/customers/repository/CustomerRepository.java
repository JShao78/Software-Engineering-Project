package de.pdbm.team18.business.customers.repository;

import de.pdbm.team18.business.customers.entity.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    List<Customer> findPage(String searchTerm, int offset, int limit);

    long count(String searchTerm);

    Optional<Customer> findById(UUID id);

    Customer save(Customer customer);
}
