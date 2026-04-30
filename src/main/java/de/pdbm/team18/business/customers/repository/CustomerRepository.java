package de.pdbm.team18.business.customers.repository;

import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Stateless
public class CustomerRepository {

    @PersistenceContext
    EntityManager em;

    public List<Customer> findByName(String name) {
        if (name == null || name.length() < 3) return List.of();
        return em.createQuery("SELECT c FROM Customer c " +
                        "WHERE c.lastName LIKE CONCAT('%', :name, '%') " +
                        "OR c.firstName LIKE CONCAT('%', :name, '%') " +
                        "OR :name IS NULL " +
                        "ORDER BY c.lastName ASC, c.firstName ASC",
                Customer.class)
                .setParameter("name", name)
                .setMaxResults(10)
                .getResultList();
    }

    public List<Customer> findPage(int offset, int limit) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);

        return em.createQuery("SELECT c FROM Customer c ORDER BY c.customerId",
                        Customer.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int count() {
        return Math.toIntExact(
                em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class)
                        .getSingleResult()
        );
    }
    public Customer create(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Customer must not be null");
        em.persist(customer);
        em.flush();
        return customer;
    }
}
