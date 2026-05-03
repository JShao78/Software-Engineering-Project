package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.orders.control.OrderController;
import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.entity.OrderItem;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.event.AjaxBehaviorEvent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Named
@ViewScoped
public class OrderBoundary implements Serializable {

    @Inject
    OrderController orderController;

    private String customerSearch;
    private String productSearch;

    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
    private int currentPage = 1;
    private Integer expandedOrderId;
    private Map<Integer, Integer> itemsCountByOrderId = Collections.emptyMap();
    private Order newOrder = new Order();
    private List<OrderItem> newOrderItems = new ArrayList<>();

    public List<Order> findPage() {
        List<Order> orders = orderController.findPage(currentPage, pageSize);
        List<Integer> orderIds = orders.stream()
                .map(Order::getOrderId)
                .filter(Objects::nonNull)
                .toList();
        itemsCountByOrderId = orderController.countItemsByOrderIds(orderIds);
        return orders;
    }

    @PostConstruct
    private void init() {
        customerSearch = "";
        productSearch = "";
    }

    public List<Customer> findCustomers() {
        return orderController.findCustomersByName(customerSearch);
    }

    public List<Product> findProducts() {
        return orderController.findProductsByName(productSearch);
    }

    public int count() {
        return orderController.count();
    }

    public int totalPages() {
        return orderController.totalPages(pageSize);
    }

    public void toggleItems(Integer orderId) {
        if (Objects.equals(expandedOrderId, orderId)) {
            expandedOrderId = null;
        } else {
            expandedOrderId = orderId;
        }
    }

    public boolean expanded(Integer orderId) {
        return Objects.equals(expandedOrderId, orderId);
    }

    public List<OrderItem> findItems(Integer orderId) {
        return orderController.findItems(orderId);
    }

    public int itemsCount(Integer orderId) {
        if (orderId == null) {
            return 0;
        }
        return itemsCountByOrderId.getOrDefault(orderId, 0);
    }

    public String statusLabel(Integer status) {
        if (status == null) return "Unknown";

        return switch (status) {
            case 1 -> "Pending";
            case 2 -> "Processing";
            case 3 -> "Rejected";
            case 4 -> "Completed";
            default -> "Unknown";
        };
    }

    public String statusClass(Integer status) {
        if (status == null) return "status-unknown";

        return switch (status) {
            case 1 -> "status-pending";
            case 2 -> "status-processing";
            case 3 -> "status-rejected";
            case 4 -> "status-completed";
            default -> "status-unknown";
        };
    }

    public boolean hasDiscount(OrderItem item) {
        return item != null
                && item.getDiscount() != null
                && item.getDiscount().compareTo(BigDecimal.ZERO) > 0;
    }

    public String discountLabel(OrderItem item) {
        if (!hasDiscount(item)) {
            return "—";
        }

        BigDecimal percent = item.getDiscount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);

        return percent + "%";
    }

    public BigDecimal itemTotal(OrderItem item) {
        if (item == null || item.getListPrice() == null || item.getQuantity() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = item.getListPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        if (item.getDiscount() != null && item.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(subtotal.multiply(item.getDiscount()));
        }

        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public void save(){
        if(newOrderItems.isEmpty()){
            addMessage(FacesMessage.SEVERITY_WARN, "Warnung", "Bitte mindestens einen Artikel hinzufügen");
            return;
        }
        try{
            orderController.create(newOrder, newOrderItems);
            addMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Bestellung erfolgreich erstellt");
            newOrder = new Order();
            newOrderItems = new ArrayList<>();
        }catch(IllegalStateException e){
            addMessage(FacesMessage.SEVERITY_ERROR, "Fehler", e.getMessage());
        }

    }
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }


    // Getter and Setter
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.clamp(currentPage, 1, Math.max(totalPages(), 1));
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void onPageSizeChange(AjaxBehaviorEvent event) {
        this.pageSize = Math.clamp(pageSize, 1, RepositoryQueryUtils.MAX_LIMIT);
        this.currentPage = 1;
    }

    public Order getNewOrder() {
        return newOrder;
    }

    public String getCustomerSearch() {
        return customerSearch;
    }

    public void setCustomerSearch(String customerSearch) {
        this.customerSearch = customerSearch;
    }

    public String getProductSearch() {
        return productSearch;
    }

    public void setProductSearch(String productSearch) {
        this.productSearch = productSearch;
    }

    public List<OrderItem> getNewOrderItems() {
        return newOrderItems;
    }
}