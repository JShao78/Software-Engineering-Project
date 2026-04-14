package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
@Stateless
public class OrderController {
    @Inject
    private OrderRepository orderRepository;

    public List<Order> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return orderRepository.findPage(offset, pageSize);
    }

    public int totalPages(int pageSize) {
        return Math.ceilDivExact(orderRepository.count(), pageSize);
    }


}
