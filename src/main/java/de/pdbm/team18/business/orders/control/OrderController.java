package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.customers.control.CustomerController;
import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.entity.OrderItem;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import de.pdbm.team18.business.stores.control.StockController;
import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.stores.entity.StockId;
import de.pdbm.team18.business.stores.entity.Store;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Stateless
@RolesAllowed({"ADMIN", "USER1", "USER2"})
public class OrderController {
    @Inject
    private OrderRepository orderRepository;
    @Inject
    private StockController stockController;

    @Inject
    private CustomerController customerController;
    @Inject
    private ProductController productController;

    public List<Order> findPage(int currentPage, int pageSize) {
        return findPage(currentPage, pageSize, "", "");
    }

    public List<Order> findPage(
            int currentPage,
            int pageSize,
            String customerSearch,
            String productSearch
    ) {
        int offset = (currentPage - 1) * pageSize;
        return orderRepository.findPage(offset, pageSize, customerSearch, productSearch);
    }

    public int totalPages(int pageSize) {
        return totalPages(pageSize, "", "");
    }

    public int totalPages(int pageSize, String customerSearch, String productSearch) {
        int count = orderRepository.count(customerSearch, productSearch);

        if (count == 0) {
            return 1;
        }

        return (int) Math.ceil((double) count / pageSize);
    }

    public int count() {
        return count("", "");
    }

    public int count(String customerSearch, String productSearch) {
        return orderRepository.count(customerSearch, productSearch);
    }

    public List<Customer> findCustomersByName(String name) {
        return customerController.findByName(name);
    }

    public List<Product>  findProductsByName(String name) {
        return productController.findByName(name);
    }

    public void createOrder(Order order) {
        orderRepository.create(order);
    }

    @RolesAllowed({"ADMIN", "USER1"})
    public List<OrderItem> findItems(Integer orderId) {
        return orderRepository.findItemsByOrderId(orderId);
    }

    @RolesAllowed({"ADMIN", "USER1"})
    public Map<Integer, Integer> countItemsByOrderIds(List<Integer> orderIds) {
        return orderRepository.countItemsByOrderIds(orderIds);
    }

    @RolesAllowed("ADMIN")
    public void create(Order order, List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Integer quantity = stockController.getQuantity(order.getStore(), orderItem.getProduct());
            if (quantity == null || quantity < orderItem.getQuantity()) {
                throw new IllegalStateException("Nicht genügend Lagerbestand für diese Bestellung");
            }
        }
        order.setOrderDate(java.time.LocalDate.now());
        order.setOrderStatus(1);
        int orderItemId = 1;
        orderRepository.create(order);
        for (OrderItem orderItem : orderItems){
            orderItem.setOrder(order);
            orderItem.getId().setItemId(orderItemId++);
            orderRepository.createItem(orderItem);
        }
    }

    @RolesAllowed({"ADMIN","USER1"})
    public void updateStatus(Order order){
        Order managedOrder = orderRepository.findById(order.getOrderId());
        if (managedOrder == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        Integer oldStatus = managedOrder.getOrderStatus();
        Integer newStatus = order.getOrderStatus();
        if (oldStatus != 4 && newStatus == 4){
            List<OrderItem> orderItems = orderRepository.findItemsByOrderId(managedOrder.getOrderId());
            for (OrderItem orderItem : orderItems) {
                Integer quantity = stockController.getQuantity(managedOrder.getStore(), orderItem.getProduct());
                if (quantity == null || quantity < orderItem.getQuantity()) {
                    throw new IllegalStateException("Nicht genügend Lagerbestand für diese Bestellung");
                }
            }
            for (OrderItem orderItem : orderItems){
                Integer quantity = stockController.getQuantity(managedOrder.getStore(), orderItem.getProduct());
                StockId stockId = new StockId(managedOrder.getStore().getStoreId(), orderItem.getProduct().getProductId());
                stockController.updateQuantity(stockId, quantity - orderItem.getQuantity());
            }
            order.setShippedDate(LocalDate.now());
        } else if (oldStatus == 4 && newStatus != 4) {
            List<OrderItem> orderItems = orderRepository.findItemsByOrderId(managedOrder.getOrderId());
            for (OrderItem orderItem : orderItems) {
                StockId stockId = new StockId(managedOrder.getStore().getStoreId(), orderItem.getProduct().getProductId());
                Integer quantity = stockController.getQuantity(managedOrder.getStore(), orderItem.getProduct());
                stockController.updateQuantity(stockId, quantity + orderItem.getQuantity());
            }
            order.setShippedDate(null);
        }

        orderRepository.update(order);
        }


}
