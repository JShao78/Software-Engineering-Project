package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.orders.repository.OrderRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

import de.pdbm.team18.business.orders.entity.*;
import jakarta.persistence.criteria.Order;

@Stateless
public class OrderControl {
    @Inject
    private OrderRepository orderRepository;

    public List<SalesOrder> findPage(UUID customerId, String orderStatus, int offset, int limit) {
        return orderRepository.findPage(customerId, orderStatus, offset, limit);
    }

    public long count(UUID customerId, String orderStatus) {
        return orderRepository.count(customerId, orderStatus);
    }
    public SalesOrder findById(UUID id) {
        return orderRepository.findById(id).orElse(null);
    }

    public SalesOrder findByIdWithPositions(UUID id) {
        return  orderRepository.findByIdWithPositions(id).orElse(null);
    }

    public SalesOrder save(SalesOrder salesOrder) {
        return orderRepository.save(salesOrder);
    }

    public boolean updateStatus(UUID orderId, String orderStatus, String shippingStatus) {
        return orderRepository.updateStatus(orderId, orderStatus, shippingStatus);
    }


}
