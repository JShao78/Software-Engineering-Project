package de.pdbm.team18.business.customers.repository;

import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Stateless
public class JpaCustomerRepository implements CustomerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Customer> findPage(String searchTerm, int offset, int limit) {
        boolean hasSearch = RepositoryQueryUtils.hasText(searchTerm);
        String searchPattern = hasSearch ? RepositoryQueryUtils.toLikePattern(searchTerm) : "%";

        TypedQuery<Customer> query = entityManager.createQuery(
            "SELECT c FROM Customer c " +
                "WHERE (:hasSearch = false " +
                "   OR LOWER(c.firstName) LIKE :searchPattern " +
                "   OR LOWER(c.lastName) LIKE :searchPattern " +
                "   OR LOWER(COALESCE(c.email, '')) LIKE :searchPattern) " +
                "ORDER BY c.lastName ASC, c.firstName ASC, c.id ASC",
            Customer.class
        );

        return query
            .setParameter("hasSearch", hasSearch)
            .setParameter("searchPattern", searchPattern)
            .setFirstResult(RepositoryQueryUtils.sanitizeOffset(offset))
            .setMaxResults(RepositoryQueryUtils.sanitizeLimit(limit))
            .getResultList();
    }

    @Override
    public long count(String searchTerm) {
        boolean hasSearch = RepositoryQueryUtils.hasText(searchTerm);
        String searchPattern = hasSearch ? RepositoryQueryUtils.toLikePattern(searchTerm) : "%";

        return entityManager.createQuery(
                "SELECT COUNT(c) FROM Customer c " +
                    "WHERE (:hasSearch = false " +
                    "   OR LOWER(c.firstName) LIKE :searchPattern " +
                    "   OR LOWER(c.lastName) LIKE :searchPattern " +
                    "   OR LOWER(COALESCE(c.email, '')) LIKE :searchPattern)",
                Long.class
            )
            .setParameter("hasSearch", hasSearch)
            .setParameter("searchPattern", searchPattern)
            .getSingleResult();
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(Customer.class, id));
    }

    @Override
    public Customer save(Customer customer) {
        if (customer.getId() != null) {
            return entityManager.merge(customer);
        }
        entityManager.persist(customer);
        return customer;
    }
}
