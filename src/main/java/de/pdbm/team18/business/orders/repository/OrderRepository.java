package de.pdbm.team18.business.orders.repository;

import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.entity.OrderItem;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class OrderRepository {
    @PersistenceContext
    EntityManager em;

    public List<Order> findPage(int offset, int limit) {
        return findPage(offset, limit, "", "");
    }

    public List<Order> findPage(
            int offset,
            int limit,
            String customerSearch,
            String productSearch
    ) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);

        Integer customerSearchNumber = parseInteger(customerSearch);
        Integer productSearchNumber = parseInteger(productSearch);
        BigDecimal productSearchDecimal = parseBigDecimal(productSearch);

        StringBuilder jpql = new StringBuilder();

        jpql.append("""
                SELECT o
                FROM Order o
                LEFT JOIN FETCH o.customer
                LEFT JOIN FETCH o.store
                WHERE 1 = 1
                """);

        if (hasText(customerSearch)) {
            jpql.append("""
                    AND (
                        LOWER(o.customer.firstName) LIKE :customerSearch
                        OR LOWER(o.customer.lastName) LIKE :customerSearch
                        OR LOWER(CONCAT(CONCAT(o.customer.firstName, ' '), o.customer.lastName)) LIKE :customerSearch
                        OR LOWER(o.store.storeName) LIKE :customerSearch
                        OR LOWER(
                            CASE o.orderStatus
                                WHEN 1 THEN 'pending offen'
                                WHEN 2 THEN 'processing in bearbeitung'
                                WHEN 3 THEN 'rejected abgelehnt'
                                WHEN 4 THEN 'completed abgeschlossen'
                                ELSE 'unknown unbekannt'
                            END
                        ) LIKE :customerSearch
                    """);

            if (customerSearchNumber != null) {
                jpql.append("""
                        OR o.orderId = :customerSearchNumber
                        OR o.orderStatus = :customerSearchNumber
                        """);
            }

            jpql.append("""
                    )
                    """);
        }

        if (hasText(productSearch)) {
            jpql.append("""
                    AND EXISTS (
                        SELECT oi
                        FROM OrderItem oi
                        JOIN oi.product p
                        WHERE oi.order = o
                        AND (
                            LOWER(p.productName) LIKE :productSearch
                    """);

            if (productSearchNumber != null) {
                jpql.append("""
                            OR p.modelYear = :productSearchNumber
                            OR oi.id.itemId = :productSearchNumber
                            OR oi.quantity = :productSearchNumber
                        """);
            }

            if (productSearchDecimal != null) {
                jpql.append("""
                            OR oi.listPrice = :productSearchDecimal
                            OR oi.discount = :productSearchDecimal
                        """);
            }

            jpql.append("""
                        )
                    )
                    """);
        }

        jpql.append("""
                ORDER BY o.orderId
                """);

        TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);

        setSearchParameters(
                query,
                customerSearch,
                productSearch,
                customerSearchNumber,
                productSearchNumber,
                productSearchDecimal
        );

        return query
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int count() {
        return count("", "");
    }

    public int count(String customerSearch, String productSearch) {
        Integer customerSearchNumber = parseInteger(customerSearch);
        Integer productSearchNumber = parseInteger(productSearch);
        BigDecimal productSearchDecimal = parseBigDecimal(productSearch);

        StringBuilder jpql = new StringBuilder();

        jpql.append("""
                SELECT COUNT(o)
                FROM Order o
                WHERE 1 = 1
                """);

        if (hasText(customerSearch)) {
            jpql.append("""
                    AND (
                        LOWER(o.customer.firstName) LIKE :customerSearch
                        OR LOWER(o.customer.lastName) LIKE :customerSearch
                        OR LOWER(CONCAT(CONCAT(o.customer.firstName, ' '), o.customer.lastName)) LIKE :customerSearch
                        OR LOWER(o.store.storeName) LIKE :customerSearch
                        OR LOWER(
                            CASE o.orderStatus
                                WHEN 1 THEN 'pending offen'
                                WHEN 2 THEN 'processing in bearbeitung'
                                WHEN 3 THEN 'rejected abgelehnt'
                                WHEN 4 THEN 'completed abgeschlossen'
                                ELSE 'unknown unbekannt'
                            END
                        ) LIKE :customerSearch
                    """);

            if (customerSearchNumber != null) {
                jpql.append("""
                        OR o.orderId = :customerSearchNumber
                        OR o.orderStatus = :customerSearchNumber
                        """);
            }

            jpql.append("""
                    )
                    """);
        }

        if (hasText(productSearch)) {
            jpql.append("""
                    AND EXISTS (
                        SELECT oi
                        FROM OrderItem oi
                        JOIN oi.product p
                        WHERE oi.order = o
                        AND (
                            LOWER(p.productName) LIKE :productSearch
                    """);

            if (productSearchNumber != null) {
                jpql.append("""
                            OR p.modelYear = :productSearchNumber
                            OR oi.id.itemId = :productSearchNumber
                            OR oi.quantity = :productSearchNumber
                        """);
            }

            if (productSearchDecimal != null) {
                jpql.append("""
                            OR oi.listPrice = :productSearchDecimal
                            OR oi.discount = :productSearchDecimal
                        """);
            }

            jpql.append("""
                        )
                    )
                    """);
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        setSearchParameters(
                query,
                customerSearch,
                productSearch,
                customerSearchNumber,
                productSearchNumber,
                productSearchDecimal
        );

        return Math.toIntExact(query.getSingleResult());
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

    public Order findById(Integer orderId) {
        return em.find(Order.class, orderId);
    }

    public Order update(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        Order managedOrder = em.merge(order);
        em.flush();
        return managedOrder;
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Integer parseInteger(String value) {
        if (!hasText(value)) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (!hasText(value)) {
            return null;
        }

        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setSearchParameters(
            TypedQuery<?> query,
            String customerSearch,
            String productSearch,
            Integer customerSearchNumber,
            Integer productSearchNumber,
            BigDecimal productSearchDecimal
    ) {
        if (hasText(customerSearch)) {
            query.setParameter(
                    "customerSearch",
                    "%" + customerSearch.trim().toLowerCase() + "%"
            );

            if (customerSearchNumber != null) {
                query.setParameter("customerSearchNumber", customerSearchNumber);
            }
        }

        if (hasText(productSearch)) {
            query.setParameter(
                    "productSearch",
                    "%" + productSearch.trim().toLowerCase() + "%"
            );

            if (productSearchNumber != null) {
                query.setParameter("productSearchNumber", productSearchNumber);
            }

            if (productSearchDecimal != null) {
                query.setParameter("productSearchDecimal", productSearchDecimal);
            }
        }
    }
}