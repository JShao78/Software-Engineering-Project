package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.orders.control.OrderControl;
import de.pdbm.team18.business.orders.entity.OrderPosition;
import de.pdbm.team18.business.orders.entity.SalesOrder;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
public class OrderBoundary implements Serializable {

    private SalesOrder order;
    private String selectedCustomerId;
    private String selectedProductId;
    private int quantity = 1;
    private boolean orderStarted = false;

    @Inject
    OrderControl controller;

    //  Getters & Setters

    public SalesOrder getOrder() { return order; }
    public void setOrder(SalesOrder order) { this.order = order; }

    public String getSelectedCustomerId() { return selectedCustomerId; }
    public void setSelectedCustomerId(String v) { this.selectedCustomerId = v; }

    public String getSelectedProductId() { return selectedProductId; }
    public void setSelectedProductId(String v) { this.selectedProductId = v; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isOrderStarted() { return orderStarted; }

    //Actions

    public void startOrder() {
        try {
            Customer customer = controller.getCustomer(UUID.fromString(selectedCustomerId));
            if (customer == null) {
                FacesContext.getCurrentInstance()
                        .addMessage("stepOneForm:customerId",
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Kein Kunde mit dieser ID gefunden.", null));
                return;
            }
            this.order = new SalesOrder(customer);
            orderStarted = true;
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance()
                    .addMessage("stepOneForm:customerId",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Kein Kunden mit dieser ID gefunden.", null));
        }
    }

    public void addPosition() {
        if (this.order == null) return;
        try {
            Product product = controller.getProduct(UUID.fromString(selectedProductId));
            if (product == null) {
                FacesContext.getCurrentInstance()
                        .addMessage("stepTwoForm:productId",
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Kein Produkt mit dieser ID gefunden.", null));
                return;
            }
            this.order.addPosition(new OrderPosition(product, quantity, product.getPrice()));
            selectedProductId = null;
            quantity = 1;
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance()
                    .addMessage("stepTwoForm:productId",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Ungültige UUID. Bitte kopieren Sie die ID aus der Produktliste.", null));
        }
    }

    public String finishOrder() {
        if (this.order.getPositions().isEmpty()) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Bitte mindestens ein Produkt hinzufügen.", null));
            return null;
        }
        this.order.setTotalCost(this.order.getTotalPrice());
        controller.save(this.order);  // ← back to save()
        this.order = null;
        orderStarted = false;
        return "bestellungen?faces-redirect=true";
    }

    //  Queries

    public SalesOrder getOrderById(UUID id) {
        return controller.findById(id);
    }

    public List<SalesOrder> getAllOrders(UUID customerId, String orderStatus, int offset, int limit) {
        return controller.findPage(customerId, orderStatus, offset, limit);
    }
}