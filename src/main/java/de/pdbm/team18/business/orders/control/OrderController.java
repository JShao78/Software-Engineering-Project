package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
@Stateless
public class OrderController {
    @Inject
    private OrderRepository orderRepository;
    public List<Order> findPage(int currentPage, int pageSize) {
        if (pageSize < 1) {
            pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
        }
        int offset = (currentPage - 1) * pageSize;
        return orderRepository.findPage(offset,pageSize);
    }

    public int getTotalPages(int pageSize) {
        return Math.ceilDivExact((int) orderRepository.count(), pageSize);
    }


}
