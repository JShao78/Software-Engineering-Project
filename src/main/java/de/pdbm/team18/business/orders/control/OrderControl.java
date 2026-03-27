package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.customers.control.CustomerControl;
import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.orders.entity.SalesOrder;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import de.pdbm.team18.business.products.control.ProductControl;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@Stateless
public class OrderControl {

    //References needed for SalesOrder and OrderPosition handling
    @Inject
    private CustomerControl customerControl;
    @Inject
    private ProductControl productControl;

    @Inject
    private OrderRepository orderRepository;

    public Product getProduct(UUID id) {
        return productControl.findById(id);
    }

    public Customer getCustomer(UUID id) {
        return customerControl.findCustomerById(id);
    }

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
