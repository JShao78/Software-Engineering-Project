package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import jakarta.inject.Inject;

import java.util.List;

public class OrderController {
    @Inject
    private OrderRepository orderRepository;
    public List<Order> findPage(int offset, int limit) {
        return orderRepository.findPage(offset, limit);
    }


}
