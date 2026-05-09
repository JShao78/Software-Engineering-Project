package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.orders.control.OrderController;
import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.orders.entity.OrderItem;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import de.pdbm.team18.business.stores.entity.Staff;
import de.pdbm.team18.business.stores.entity.Store;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Named
@ViewScoped
public class OrderBoundary implements Serializable {

    @Inject
    OrderController orderController;

    private String customerSearch;
    private String productSearch;

    private String newCustomerSearch;
    private String newProductSearch;

    private int pageSize;
    private int currentPage;
    private Integer expandedOrderId;
    private Map<Integer, Integer> itemsCountByOrderId;
    private Order newOrder;
    private Order editOrder;
    private OrderItem newOrderItem; //TODO naming?!
    private List<OrderItem> newOrderItems;

    public void debug() { //TODO remove
        if (newOrder.getCustomer() == null) newOrder.setCustomer(new Customer());
        System.out.println("Status:");
        System.out.println("newCustomerSearch: " + newCustomerSearch);
        System.out.println("newProductSearch: " + newProductSearch);
        System.out.println("Produkt: " + newOrderItem.getProduct().getProductName());
        System.out.println("newOrderItems:");
        for (OrderItem item : newOrderItems) System.out.println(item.getProduct().getProductName());
        System.out.println(newOrder.getCustomer());
        System.out.println("Customer: " + newOrder.getCustomer().getFirstName() + " " + newOrder.getCustomer().getLastName());
    }

    @PostConstruct
    private void init() {
        customerSearch = "";
        productSearch = "";
        newCustomerSearch = "";
        newProductSearch = "";
        pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
        currentPage = 1;
        expandedOrderId = null;
        itemsCountByOrderId = Collections.emptyMap();
        newOrder = new Order();
        editOrder = new Order();
        newOrderItems = new ArrayList<>();
        newOrderItem  = new OrderItem();
        newOrderItem.setOrder(newOrder);
    }

    public List<Order> findPage() {
        List<Order> orders = orderController.findPage(
                currentPage,
                pageSize,
                customerSearch,
                productSearch
        );

        if (canViewOrderPositions()) {
            List<Integer> orderIds = orders.stream()
                    .map(Order::getOrderId)
                    .filter(Objects::nonNull)
                    .toList();
            itemsCountByOrderId = orderController.countItemsByOrderIds(orderIds);
        } else {
            itemsCountByOrderId = Collections.emptyMap();
            expandedOrderId = null;
        }
        debug(); //TODO remove
        return orders;
    }

    public int count() {
        return orderController.count(customerSearch, productSearch);
    }

    public int totalPages() {
        return orderController.totalPages(pageSize, customerSearch, productSearch);
    }

    public String applySearch() {
        currentPage = 1;
        expandedOrderId = null;
        return null;
    }

    public String resetSearch() {
        customerSearch = "";
        productSearch = "";
        currentPage = 1;
        expandedOrderId = null;
        return null;
    }

    public List<Customer> findCustomers() {
        return orderController.findCustomersByName(newCustomerSearch);
    }

    public List<Product> findProducts() {
        return orderController.findProductsByName(newProductSearch);
    }

    public List<Staff> findStaff() {
        return orderController.findStaff();
    }

    public List<Store> findStores() {
        return orderController.findStores();
    }

    public void saveNewOrderItem() {
        if (newOrderItem.getProduct() == null || newOrderItem.getQuantity() == null || newOrderItem.getQuantity() == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pflichtfelder nicht erfüllt",
                            "Produkt und Menge müssen angegeben sein."));
            return;
        }
        newOrderItems.add(newOrderItem);
        newOrderItem = new OrderItem();
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

    public boolean canViewOrderPositions() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return false;
        }

        return facesContext.getExternalContext().isUserInRole("ADMIN")
                || facesContext.getExternalContext().isUserInRole("USER1");
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

    public void create() {
        if (newOrderItems.isEmpty()) {
            return;
        }
        orderController.create(newOrder, newOrderItems);

        newOrder = new Order();
        newOrderItems = new ArrayList<>();
    }

    public boolean prepareEdit(Order orderToEdit) {
        if (orderToEdit == null || orderToEdit.getOrderId() == null) {
            cancelEdit();
            return false;
        }

        this.editOrder = orderToEdit;
        return true;
    }

    public boolean saveEdit() {
        if (editOrder.getOrderId() == null) {
            return false;
        }
        try {
            orderController.edit(editOrder);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Erfolg: ", "Bestellung erfolgreich aktualisiert."));
            cancelEdit();
            return true;

        } catch (Exception e) {
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            if (rootCause instanceof IllegalStateException || rootCause instanceof IllegalArgumentException) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler: ", rootCause.getMessage()));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Systemfehler: ", "Ein unerwarteter Fehler ist aufgetreten."));
                e.printStackTrace();
            }
            return false;
        }
    }

    public void cancelEdit() {
        this.editOrder = new Order();
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
        this.expandedOrderId = null;
    }

    public Order getNewOrder() {
        return newOrder;
    }

    public void setNewOrder(Order newOrder) {
        this.newOrder = newOrder;
    }

    public List<OrderItem> getNewOrderItems() {
        return newOrderItems;
    }

    public Order getEditOrder() {
        return editOrder;
    }

    public void setNewOrderItems(List<OrderItem> newOrderItems) {
        this.newOrderItems = newOrderItems;
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

    public String getNewCustomerSearch() {
        return newCustomerSearch;
    }

    public void setNewCustomerSearch(String newCustomerSearch) {
        try {
            int id = Integer.parseInt(newCustomerSearch);
            Customer customer = orderController.findCustomerById(id);
            if (customer != null) newOrder.setCustomer(customer);
        } catch (NumberFormatException ignored) {}
        this.newCustomerSearch = newCustomerSearch;
    }

    public String getNewProductSearch() {
        return newProductSearch;
    }

    public void setNewProductSearch(String newProductSearch) {
        try {
            int id = Integer.parseInt(newProductSearch);
            Product product = orderController.findProductById(id);
            if (product != null) newOrderItem.setProduct(product);
        } catch (NumberFormatException ignored) {}
        this.newProductSearch = newProductSearch;
    }

    public OrderItem getNewOrderItem() {
        return newOrderItem;
    }

    public void setNewOrderItem(OrderItem newOrderItem) {
        this.newOrderItem = newOrderItem;
    }
}
