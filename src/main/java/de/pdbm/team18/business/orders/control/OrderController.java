package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.entity.OrderItem;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@Stateless
public class OrderController {
    @Inject
    private OrderRepository orderRepository;

    public List<Order> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return orderRepository.findPage(offset, pageSize);
    }

    public int totalPages(int pageSize) {
        int count = orderRepository.count();
        return (int) Math.ceil((double) count / pageSize);
    }

    public int count() {
        return orderRepository.count();
    }

    public List<OrderItem> findItems(Integer orderId) {
        return orderRepository.findItemsByOrderId(orderId);
    }

    public int countItems(Integer orderId) {
        return orderRepository.countItemsByOrderId(orderId);
    }

    public Map<Integer, Integer> countItemsByOrderIds(List<Integer> orderIds) {
        return orderRepository.countItemsByOrderIds(orderIds);
    }
}
