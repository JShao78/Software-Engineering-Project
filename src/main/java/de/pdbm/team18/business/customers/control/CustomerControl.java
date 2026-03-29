package de.pdbm.team18.business.customers.control;

import de.pdbm.team18.business.customers.repository.CustomerRepository;
import jakarta.ejb.Stateless;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;

@Stateless
public class CustomerControl {
    @Inject
    private CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager em;

    public List<Customer> findPage(String searchTerm, int offset, int limit) {
        return em.createQuery("SELECT c FROM Customer c", Customer.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
    public void saveCustomer(Customer customer) {
        em.persist(customer);
    }
    public long countCustomers(String searchTerm) {
        return customerRepository.count(searchTerm);
    }

    public Customer findCustomerById(UUID id) {
        return customerRepository.findById(id).orElse(null);
    }


}
