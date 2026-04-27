package de.pdbm.team18.business.customers.repository;

import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class CustomerRepository {

    @PersistenceContext
    EntityManager em;

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

    public void delete(Integer customerId) {
        Customer customer = em.find(Customer.class, customerId);
        if (customer != null) {
            em.remove(em.merge(customer));
        }
    }

    public Customer findById(Integer customerId) {
        return em.find(Customer.class, customerId);
    }

    public boolean hasOrders(Integer customerId) {
        long count = em.createQuery("SELECT COUNT(o) FROM Order o WHERE o.customer.customerId = :customerId", Long.class)
                .setParameter("customerId", customerId)
                .getSingleResult();
        return count > 0;
    }
}
