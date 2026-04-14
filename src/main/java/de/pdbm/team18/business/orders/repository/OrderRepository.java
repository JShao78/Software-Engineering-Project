package de.pdbm.team18.business.orders.repository;

import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class OrderRepository {
    @PersistenceContext
    EntityManager em;

    public List<Order> findPage(int offset, int limit) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);
        return em.createQuery("SELECT DISTINCT o FROM Order o " +
                                "LEFT JOIN FETCH o.customer " +
                                "LEFT JOIN FETCH o.store " +
                                "LEFT JOIN FETCH o.orderItems " +
                                "ORDER BY o.orderId ",
                                     Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int count() {
        return Math.toIntExact(em.createQuery("SELECT COUNT(o) FROM Order o", Long.class).getSingleResult());
    }


}
