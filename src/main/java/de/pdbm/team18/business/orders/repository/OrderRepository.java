package de.pdbm.team18.business.orders.repository;

import de.pdbm.team18.business.orders.entity.SalesOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    List<SalesOrder> findPage(UUID customerId, String orderStatus, int offset, int limit);

    long count(UUID customerId, String orderStatus);

    Optional<SalesOrder> findById(UUID id);

    Optional<SalesOrder> findByIdWithPositions(UUID id);

    SalesOrder save(SalesOrder order);

    boolean updateStatus(UUID orderId, String orderStatus, String shippingStatus);
}
