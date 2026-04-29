package de.pdbm.team18.business.orders.repository;

import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.entity.OrderItem;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class OrderRepository {
    @PersistenceContext
    EntityManager em;

    public List<Order> findPage(int offset, int limit) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);

        return em.createQuery("""
                SELECT o
                FROM Order o
                LEFT JOIN FETCH o.customer
                LEFT JOIN FETCH o.store
                ORDER BY o.orderId
                """, Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int count() {
        return Math.toIntExact(
                em.createQuery("SELECT COUNT(o) FROM Order o", Long.class)
                        .getSingleResult()
        );
    }

    public List<OrderItem> findItemsByOrderId(Integer orderId) {
        return em.createQuery("""
                SELECT oi
                FROM OrderItem oi
                JOIN FETCH oi.product
                WHERE oi.order.orderId = :orderId
                ORDER BY oi.id.itemId
                """, OrderItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public Map<Integer, Integer> countItemsByOrderIds(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> rows = em.createQuery("""
                        SELECT oi.order.orderId, COUNT(oi)
                        FROM OrderItem oi
                        WHERE oi.order.orderId IN :orderIds
                        GROUP BY oi.order.orderId
                        """, Object[].class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Integer, Integer> countsByOrderId = new HashMap<>();
        for (Object[] row : rows) {
            Integer orderId = (Integer) row[0];
            int count = Math.toIntExact((Long) row[1]);
            countsByOrderId.put(orderId, count);
        }

        return countsByOrderId;
    }

    public Order create(Order order) {
        em.persist(order);
        em.flush();
        return order;
    }

    public OrderItem createItem(OrderItem orderItem) {
        em.persist(orderItem);
        em.flush();
        return orderItem;
    }
}
