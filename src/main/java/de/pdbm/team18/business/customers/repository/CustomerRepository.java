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

}
