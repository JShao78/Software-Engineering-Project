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

    public List<Order> findPage(int offset, int limit, String customerSearch, String productSearch) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);

        SearchQuery searchQuery = buildSearchQuery(customerSearch, productSearch);
        String jpql = """
                SELECT o
                FROM Order o
                LEFT JOIN FETCH o.customer
                LEFT JOIN FETCH o.store
                """
                + searchQuery.whereClause()
                + """
                ORDER BY o.orderId
                """;

        TypedQuery<Order> query = em.createQuery(jpql, Order.class);
        setParameters(query, searchQuery.params());

        return query
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int count(String customerSearch, String productSearch) {
        SearchQuery searchQuery = buildSearchQuery(customerSearch, productSearch);
        String jpql = """
                SELECT COUNT(o)
                FROM Order o
                """
                + searchQuery.whereClause();

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        setParameters(query, searchQuery.params());

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
        Order managedOrder = em.find(Order.class, order.getOrderId());
        if (managedOrder == null) {
            throw new IllegalArgumentException("Order not found");
        }
        managedOrder.setOrderStatus(order.getOrderStatus());
        managedOrder.setShippedDate(order.getShippedDate());
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

    private SearchQuery buildSearchQuery(String customerSearch, String productSearch) { //TODO Debugging needed
        String normalizedCustomerSearch = normalizeSearch(customerSearch);
        String normalizedProductSearch = normalizeSearch(productSearch);
        StringBuilder whereClause = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        Integer customerSearchNumber = parseInteger(normalizedCustomerSearch);
        Integer productSearchNumber = parseInteger(normalizedProductSearch);
        BigDecimal productSearchDecimal = parseBigDecimal(normalizedProductSearch);

        if (hasText(normalizedCustomerSearch)) {
            whereClause.append("""
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
            params.put("customerSearch", "%" + normalizedCustomerSearch + "%");

            if (customerSearchNumber != null) {
                whereClause.append("""
                        OR o.orderId = :customerSearchNumber
                        OR o.orderStatus = :customerSearchNumber
                        """);
                params.put("customerSearchNumber", customerSearchNumber);
            }

            whereClause.append("""
                    )
                    """);
        }

        if (hasText(normalizedProductSearch)) {
            whereClause.append("""
                    AND EXISTS (
                        SELECT oi
                        FROM OrderItem oi
                        JOIN oi.product p
                        WHERE oi.order = o
                        AND (
                            LOWER(p.productName) LIKE :productSearch
                    """);
            params.put("productSearch", "%" + normalizedProductSearch + "%");

            if (productSearchNumber != null) {
                whereClause.append("""
                            OR p.modelYear = :productSearchNumber
                            OR oi.id.itemId = :productSearchNumber
                            OR oi.quantity = :productSearchNumber
                        """);
                params.put("productSearchNumber", productSearchNumber);
            }

            if (productSearchDecimal != null) {
                whereClause.append("""
                            OR oi.listPrice = :productSearchDecimal
                            OR oi.discount = :productSearchDecimal
                        """);
                params.put("productSearchDecimal", productSearchDecimal);
            }

            whereClause.append("""
                        )
                    )
                    """);
        }

        return new SearchQuery(whereClause.toString(), params);
    }

    private void setParameters(TypedQuery<?> query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private String normalizeSearch(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private record SearchQuery(
            String whereClause,
            Map<String, Object> params
    ) {
    }
}