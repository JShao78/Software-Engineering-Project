package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.customers.control.CustomerController;
import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.entity.OrderItem;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import de.pdbm.team18.business.stores.control.StockController;
import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@Stateless
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

    public List<OrderItem> findItems(Integer orderId) {
        return orderRepository.findItemsByOrderId(orderId);
    }

    public Map<Integer, Integer> countItemsByOrderIds(List<Integer> orderIds) {
        return orderRepository.countItemsByOrderIds(orderIds);
    }

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
}
