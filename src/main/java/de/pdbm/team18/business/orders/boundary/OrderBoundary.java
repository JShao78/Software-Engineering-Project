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
import java.util.Iterator;
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
    private String newStoreSearch;
    private String newStaffSearch;

    private int pageSize;
    private int currentPage;
    private int totalPages;

    private Integer expandedOrderId;
    private Map<Integer, Integer> itemsCountByOrderId;

    private Order newOrder;
    private Order editOrder;
    private Integer originalEditStatus;
    private OrderItem newOrderItem;
    private List<OrderItem> newOrderItems;

    @PostConstruct
    private void init() {
        customerSearch = "";
        productSearch = "";

        pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
        currentPage = 1;
        expandedOrderId = null;
        itemsCountByOrderId = Collections.emptyMap();

        editOrder = new Order();

        resetNewOrder();
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

        return orders;
    }

    public int count() {
        int c = orderController.count(customerSearch, productSearch);
        totalPages = Math.max(Math.ceilDivExact(c, pageSize), 1);
        return c;
    }

    public int totalPages() {
        return totalPages;
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

    public void resetNewOrder() {
        newOrder = new Order();
        newOrderItems = new ArrayList<>();

        newOrderItem = new OrderItem();
        newOrderItem.setOrder(newOrder);
        newOrderItem.setDiscount(BigDecimal.ZERO);

        newCustomerSearch = "";
        newProductSearch = "";
        newStoreSearch = "";
        newStaffSearch = "";
    }

    public void resetCreateModal() {
        resetNewOrder();
        clearFacesMessages();
    }

    private void clearFacesMessages() {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext == null) {
            return;
        }

        Iterator<FacesMessage> messages = facesContext.getMessages();

        while (messages.hasNext()) {
            messages.next();
            messages.remove();
        }
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
        clearFacesMessages();

        Product selectedProduct = resolveProduct(newProductSearch);

        if (selectedProduct != null) {
            newOrderItem.setProduct(selectedProduct);
            newOrderItem.setListPrice(selectedProduct.getListPrice());
        }

        if (newOrderItem.getProduct() == null
                || newOrderItem.getQuantity() == null
                || newOrderItem.getQuantity() <= 0) {

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Pflichtfelder nicht erfüllt",
                            "Produkt und Menge müssen angegeben sein."
                    ));
            return;
        }

        if (newOrderItem.getListPrice() == null && newOrderItem.getProduct() != null) {
            newOrderItem.setListPrice(newOrderItem.getProduct().getListPrice());
        }

        if (newOrderItem.getDiscount() == null) {
            newOrderItem.setDiscount(BigDecimal.ZERO);
        }

        boolean productAlreadyAdded = newOrderItems.stream()
                .anyMatch(item ->
                        item != null
                                && item.getProduct() != null
                                && newOrderItem.getProduct() != null
                                && Objects.equals(
                                item.getProduct().getProductId(),
                                newOrderItem.getProduct().getProductId()
                        )
                );

        if (productAlreadyAdded) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Produkt bereits vorhanden",
                            "Das ausgewählte Produkt ist bereits in der Bestellung vorhanden."
                    ));
            return;
        }

        newOrderItem.setOrder(newOrder);
        newOrderItems.add(newOrderItem);

        newOrderItem = new OrderItem();
        newOrderItem.setOrder(newOrder);
        newOrderItem.setDiscount(BigDecimal.ZERO);

        newProductSearch = "";
    }

    public String removeNewOrderItemByProductId(Integer productId) {
        if (productId != null && newOrderItems != null) {
            newOrderItems.removeIf(item ->
                    item != null
                            && item.getProduct() != null
                            && Objects.equals(item.getProduct().getProductId(), productId)
            );
        }

        clearFacesMessages();
        return null;
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
        if (item == null
                || item.getListPrice() == null
                || item.getQuantity() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = item.getListPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        if (item.getDiscount() != null
                && item.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(subtotal.multiply(item.getDiscount()));
        }

        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public void create() {
        clearFacesMessages();

        Customer selectedCustomer = resolveCustomer(newCustomerSearch);
        Store selectedStore = resolveStore(newStoreSearch);
        Staff selectedStaff = resolveStaff(newStaffSearch);

        if (selectedCustomer != null) {
            newOrder.setCustomer(selectedCustomer);
        }

        if (selectedStore != null) {
            newOrder.setStore(selectedStore);
        }

        if (selectedStaff != null) {
            newOrder.setStaff(selectedStaff);
        }

        if (newOrder.getCustomer() == null
                || newOrder.getStore() == null
                || newOrder.getStaff() == null
                || newOrder.getRequiredDate() == null) {

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Pflichtfelder nicht erfüllt",
                            "Kunde, Niederlassung, Mitarbeiter und Lieferdatum müssen angegeben sein."
                    ));
            return;
        }

        if (newOrderItems.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Keine Positionen",
                            "Mindestens ein Produkt muss zur Bestellung hinzugefügt werden."
                    ));
            return;
        }
        try {
            orderController.create(newOrder, newOrderItems);
            resetNewOrder();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Erfolg",
                            "Bestellung erfolgreich erstellt"
                    ));

        } catch (IllegalStateException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Lagerbestand",
                            e.getMessage()
                    ));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Systemfehler",
                            "Bestellung konnte nicht erstellt werden."
                    ));
            e.printStackTrace();
        }
    }

    public boolean prepareEdit(Order orderToEdit) {
        if (orderToEdit == null || orderToEdit.getOrderId() == null) {
            cancelEdit();
            return false;
        }

        this.editOrder = orderToEdit;
        this.originalEditStatus = orderToEdit.getOrderStatus();
        return true;
    }

    public boolean saveEdit() {
        if (editOrder.getOrderId() == null) {
            return false;
        }
        try {
            orderController.edit(editOrder);
            originalEditStatus = editOrder.getOrderStatus();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Erfolg: ",
                            "Bestellung erfolgreich aktualisiert."
                    ));

            return true;

        } catch (Exception e) {
            editOrder.setOrderStatus(originalEditStatus);

            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            if (rootCause instanceof IllegalStateException
                    || rootCause instanceof IllegalArgumentException) {

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                "Fehler: ",
                                rootCause.getMessage()
                        ));

            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                "Systemfehler: ",
                                "Ein unerwarteter Fehler ist aufgetreten."
                        ));
                e.printStackTrace();
            }
            return false;
        }
    }

    public void cancelEdit() {
        this.editOrder = new Order();
    }

    private Integer parseLeadingInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String trimmed = value.trim();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);

            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                break;
            }
        }

        if (number.length() == 0) {
            return null;
        }

        try {
            return Integer.parseInt(number.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private Customer resolveCustomer(String value) {
        Integer id = parseLeadingInteger(value);

        if (id != null) {
            Customer customer = orderController.findCustomerById(id);
            if (customer != null) {
                return customer;
            }
        }

        String search = normalize(value);

        if (search.isEmpty()) {
            return null;
        }

        List<Customer> customers = orderController.findCustomersByName(value);
        List<Customer> exactMatches = customers.stream()
                .filter(customer -> normalize(customer.getFirstName() + " " + customer.getLastName()).equals(search))
                .toList();

        if (exactMatches.size() == 1) {
            return exactMatches.getFirst();
        }

        if (exactMatches.size() > 1) {
            return null;
        }

        return customers.size() == 1 ? customers.getFirst() : null;
    }

    private Product resolveProduct(String value) {
        Integer id = parseLeadingInteger(value);

        if (id != null) {
            Product product = orderController.findProductById(id);
            if (product != null) {
                return product;
            }
        }

        String search = normalize(value);

        if (search.isEmpty()) {
            return null;
        }

        List<Product> products = orderController.findProductsByName(value);

        return products.stream()
                .filter(product -> normalize(product.getProductName()).equals(search))
                .findFirst()
                .orElseGet(() -> products.stream().findFirst().orElse(null));
    }

    private Store resolveStore(String value) {
        String search = normalize(value);

        if (search.isEmpty()) {
            return null;
        }

        return findStores().stream()
                .filter(store -> normalize(store.getStoreName()).equals(search))
                .findFirst()
                .orElseGet(() -> findStores().stream()
                        .filter(store -> normalize(store.getStoreName()).contains(search))
                        .findFirst()
                        .orElse(null));
    }

    private Staff resolveStaff(String value) {
        List<Staff> staffs = findStaff();
        Integer id = parseLeadingInteger(value);

        if (id != null) {
            return staffs.stream()
                    .filter(staff -> staff.getStaffId() == id)
                    .findFirst()
                    .orElse(null);
        }

        String search = normalize(value);

        if (search.isEmpty()) {
            return null;
        }

        return staffs.stream()
                .filter(staff -> normalize(staff.getFirstName() + " " + staff.getLastName()).equals(search))
                .findFirst()
                .orElseGet(() -> staffs.stream()
                        .filter(staff -> normalize(staff.getFirstName() + " " + staff.getLastName()).contains(search))
                        .findFirst()
                        .orElse(null));
    }

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

    public void setNewOrderItems(List<OrderItem> newOrderItems) {
        this.newOrderItems = newOrderItems;
    }

    public Order getEditOrder() {
        return editOrder;
    }

    public Integer getOriginalEditStatus() {
        return originalEditStatus;
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
        this.newCustomerSearch = newCustomerSearch;

        Customer customer = resolveCustomer(newCustomerSearch);
        if (customer != null) {
            newOrder.setCustomer(customer);
        }
    }

    public String getNewProductSearch() {
        return newProductSearch;
    }

    public void setNewProductSearch(String newProductSearch) {
        this.newProductSearch = newProductSearch;

        Product product = resolveProduct(newProductSearch);
        if (product != null) {
            newOrderItem.setProduct(product);
            newOrderItem.setListPrice(product.getListPrice());
        }
    }

    public String getNewStoreSearch() {
        return newStoreSearch;
    }

    public void setNewStoreSearch(String newStoreSearch) {
        this.newStoreSearch = newStoreSearch;

        Store store = resolveStore(newStoreSearch);
        if (store != null) {
            newOrder.setStore(store);
        }
    }

    public String getNewStaffSearch() {
        return newStaffSearch;
    }

    public void setNewStaffSearch(String newStaffSearch) {
        this.newStaffSearch = newStaffSearch;

        Staff staff = resolveStaff(newStaffSearch);
        if (staff != null) {
            newOrder.setStaff(staff);
        }
    }

    public OrderItem getNewOrderItem() {
        return newOrderItem;
    }

    public void setNewOrderItem(OrderItem newOrderItem) {
        this.newOrderItem = newOrderItem;
    }
}
