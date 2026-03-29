package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.orders.control.OrderControl;
import de.pdbm.team18.business.orders.entity.OrderPosition;
import de.pdbm.team18.business.orders.entity.SalesOrder;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.UUID;

@Named
@RequestScoped
public class OrderBoundary {

    //Property to temporary store the SalesOrder that is being created.
    private SalesOrder order;

    private UUID selectedCustomerId;
    private UUID selectedProductId;
    private int quantity;

    public UUID getSelectedCustomerId() { return selectedCustomerId; }
    public void setSelectedCustomerId(UUID selectedCustomerId) { this.selectedCustomerId = selectedCustomerId; }

    public UUID getSelectedProductId() { return selectedProductId; }
    public void setSelectedProductId(UUID selectedProductId) { this.selectedProductId = selectedProductId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String createAndSave() {
        addOrder(selectedCustomerId);
        OrderPosition(selectedProductId, quantity);
        saveOrder();
        return "bestellungen?faces-redirect=true";
    }

    public void setOrder(SalesOrder order) { this.order = order; }
    public SalesOrder getOrder() { return this.order; }

    @Inject
    OrderControl controller;

    /**
     * Returns the order with the provided UUID.
     * @param id the UUID.
     * @return the Order with the UUID or null if none exists.
     */
    public SalesOrder getOrderById(UUID id) {
        return controller.findById(id);
    }

    /**
     * Returns a page of Orders by the provided Customer and matching the provided order status.
     * @param customerId the UUID of the Customer.
     * @param orderStatus the status to match Orders.
     * @param offset the page number.
     * @param limit the page size.
     * @return a list representing the page.
     */
    public List<SalesOrder> getAllOrders(UUID customerId, String orderStatus, int offset, int limit) {
        return controller.findPage(customerId, orderStatus, offset, limit);
    }

    /**
     * Adds a new OrderPosition to the current SalesOrder. Does nothing if there is no current Order.
     * @param productId the ID of the Product to be added.
     * @param quantity the quantity of the Product.
     */
    public void OrderPosition(UUID productId, int quantity) {
        if (this.order == null) return;
        Product product = controller.getProduct(productId);
        this.order.addPosition(new OrderPosition(product, quantity, product.getPrice()));
    }

    /**
     * Creates a temporary SalesOrder entity to add OrderPositions to. This method does NOT save persistently.
     * @param customerID the ID of the Customer ordering.
     */
    public void addOrder(UUID customerID) {
        this.order = new SalesOrder(controller.getCustomer(customerID));
    }

    /**
     * Saves the current SalesOrder.
     */
    public void saveOrder() {
        controller.save(this.order);
        this.order = null;
    }

}
