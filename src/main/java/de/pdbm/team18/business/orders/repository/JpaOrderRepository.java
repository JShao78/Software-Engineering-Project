package de.pdbm.team18.business.orders.repository;

import de.pdbm.team18.business.orders.entity.SalesOrder;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Stateless
public class JpaOrderRepository implements OrderRepository {
    private static final Set<String> ALLOWED_ORDER_STATUSES =
        Set.of("pending", "paid", "cancelled", "completed");
    private static final Set<String> ALLOWED_SHIPPING_STATUSES =
        Set.of("not_shipped", "processing", "shipped", "delivered", "returned");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SalesOrder> findPage(UUID customerId, String orderStatus, int offset, int limit) {
        boolean hasStatus = RepositoryQueryUtils.hasText(orderStatus);
        String normalizedStatus = hasStatus ? RepositoryQueryUtils.normalizeLowercase(orderStatus) : "";

        List<UUID> ids = entityManager.createQuery(
            "SELECT o.id FROM SalesOrder o " +
                "WHERE (:customerId IS NULL OR o.customer.id = :customerId) " +
                "  AND (:hasStatus = false OR o.orderStatus = :orderStatus) " +
                "ORDER BY o.orderDate DESC, o.id DESC",
            UUID.class
        )
            .setParameter("customerId", customerId)
            .setParameter("hasStatus", hasStatus)
            .setParameter("orderStatus", normalizedStatus)
            .setFirstResult(RepositoryQueryUtils.sanitizeOffset(offset))
            .setMaxResults(RepositoryQueryUtils.sanitizeLimit(limit))
            .getResultList();

        if (ids.isEmpty()) {
            return List.of();
        }

        List<SalesOrder> orders = entityManager.createQuery(
            "SELECT DISTINCT o FROM SalesOrder o " +
                "JOIN FETCH o.customer " +
                "LEFT JOIN FETCH o.positions " +
                "WHERE o.id IN :ids " +
                "ORDER BY o.orderDate DESC, o.id DESC",
            SalesOrder.class
        )
            .setParameter("ids", ids)
            .getResultList();

        Map<UUID, SalesOrder> byId = new LinkedHashMap<>();
        for (SalesOrder order : orders) {
            byId.put(order.getId(), order);
        }

        return ids.stream()
            .map(byId::get)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    @Override
    public long count(UUID customerId, String orderStatus) {
        boolean hasStatus = RepositoryQueryUtils.hasText(orderStatus);
        String normalizedStatus = hasStatus ? RepositoryQueryUtils.normalizeLowercase(orderStatus) : "";

        return entityManager.createQuery(
                "SELECT COUNT(o) FROM SalesOrder o " +
                    "WHERE (:customerId IS NULL OR o.customer.id = :customerId) " +
                    "  AND (:hasStatus = false OR o.orderStatus = :orderStatus)",
                Long.class
            )
            .setParameter("customerId", customerId)
            .setParameter("hasStatus", hasStatus)
            .setParameter("orderStatus", normalizedStatus)
            .getSingleResult();
    }

    @Override
    public Optional<SalesOrder> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(SalesOrder.class, id));
    }

    @Override
    public Optional<SalesOrder> findByIdWithPositions(UUID id) {
        return entityManager.createQuery(
                "SELECT DISTINCT o FROM SalesOrder o " +
                    "LEFT JOIN FETCH o.customer " +
                    "LEFT JOIN FETCH o.positions op " +
                    "LEFT JOIN FETCH op.product " +
                    "WHERE o.id = :id",
                SalesOrder.class
            )
            .setParameter("id", id)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public SalesOrder save(SalesOrder order) {
        if (order.getId() != null) {
            return entityManager.merge(order);
        }
        entityManager.persist(order);
        return order;
    }

    @Override
    public boolean updateStatus(UUID orderId, String orderStatus, String shippingStatus) {
        if (!RepositoryQueryUtils.hasText(orderStatus) || !RepositoryQueryUtils.hasText(shippingStatus)) {
            throw new IllegalArgumentException("orderStatus and shippingStatus are required");
        }

        String normalizedOrderStatus = RepositoryQueryUtils.normalizeLowercase(orderStatus);
        String normalizedShippingStatus = RepositoryQueryUtils.normalizeLowercase(shippingStatus);

        if (!ALLOWED_ORDER_STATUSES.contains(normalizedOrderStatus)) {
            throw new IllegalArgumentException("Unsupported order status: " + orderStatus);
        }
        if (!ALLOWED_SHIPPING_STATUSES.contains(normalizedShippingStatus)) {
            throw new IllegalArgumentException("Unsupported shipping status: " + shippingStatus);
        }

        SalesOrder order = entityManager.find(SalesOrder.class, orderId);
        if (order == null) {
            return false;
        }

        order.setOrderStatus(normalizedOrderStatus);
        order.setShippingStatus(normalizedShippingStatus);
        return true;
    }
}
