package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.orders.control.OrderControl;
import de.pdbm.team18.business.orders.entity.SalesOrder;
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
        if (selectedCustomerId == null || selectedCustomerId.isBlank()) {
            addFieldError("stepOneForm:customerId", "Bitte eine Kunden-ID eingeben.");
            return;
        }

        try {
            this.order = controller.startOrder(UUID.fromString(selectedCustomerId));
            orderStarted = true;
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() != null ? e.getMessage() :
                    "Ungültige Kunden-ID. Bitte die ID aus der Kundenliste kopieren.";
            addFieldError("stepOneForm:customerId", message);
        }
    }

    public void addPosition() {
        if (this.order == null) {
            addGlobalError("Bitte zuerst eine Bestellung starten.");
            return;
        }
        if (selectedProductId == null || selectedProductId.isBlank()) {
            addFieldError("stepTwoForm:productId", "Bitte eine Produkt-ID eingeben.");
            return;
        }

        try {
            controller.addProductToOrder(this.order, UUID.fromString(selectedProductId), quantity);
            selectedProductId = null;
            quantity = 1;
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() != null ? e.getMessage() :
                    "Ungültige Eingabe. Bitte prüfen Sie Produkt-ID und Menge.";
            String fieldId = message.toLowerCase().contains("menge")
                    ? "stepTwoForm:quantity"
                    : "stepTwoForm:productId";
            addFieldError(fieldId, message);
        }
    }

    public String finishOrder() {
        if (this.order == null) {
            addGlobalError("Keine aktive Bestellung vorhanden.");
            return null;
        }
        try {
            controller.save(this.order);
            this.order = null;
            orderStarted = false;
            return "bestellungen?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            addGlobalError(e.getMessage());
            return null;
        } catch (RuntimeException e) {
            addGlobalError("Bestellung konnte nicht gespeichert werden. Bitte Eingaben prüfen und erneut versuchen.");
            return null;
        }
    }

    //  Queries

    public SalesOrder getOrderById(UUID id) {
        return controller.findById(id);
    }

    public List<SalesOrder> getAllOrders(UUID customerId, String orderStatus, int offset, int limit) {
        return controller.findPage(customerId, orderStatus, offset, limit);
    }

    private void addFieldError(String fieldId, String message) {
        FacesContext.getCurrentInstance()
                .addMessage(fieldId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private void addGlobalError(String message) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
}
