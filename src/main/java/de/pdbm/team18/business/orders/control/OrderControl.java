package de.pdbm.team18.business.orders.control;

import de.pdbm.team18.business.customers.control.CustomerControl;
import de.pdbm.team18.business.customers.entity.Customer;
import de.pdbm.team18.business.orders.entity.OrderPosition;
import de.pdbm.team18.business.orders.entity.SalesOrder;
import de.pdbm.team18.business.orders.repository.OrderRepository;
import de.pdbm.team18.business.products.control.ProductControl;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public SalesOrder startOrder(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Kunden-ID fehlt.");
        }
        Customer customer = customerControl.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Kein Kunde mit dieser ID gefunden.");
        }
        return new SalesOrder(customer);
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

    public void addProductToOrder(SalesOrder salesOrder, UUID productId, int quantity) {
        if (salesOrder == null) {
            throw new IllegalArgumentException("Bitte zuerst eine Bestellung starten.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Produkt-ID fehlt.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Die Menge muss mindestens 1 sein.");
        }

        Product product = productControl.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Kein Produkt mit dieser ID gefunden.");
        }

        for (OrderPosition existing : salesOrder.getPositions()) {
            if (existing.getProduct() != null && productId.equals(existing.getProduct().getId())) {
                existing.setQuantity(existing.getQuantity() + quantity);
                salesOrder.setTotalCost(salesOrder.getTotalPrice());
                return;
            }
        }

        salesOrder.addPosition(new OrderPosition(product, quantity, product.getPrice()));
        salesOrder.setTotalCost(salesOrder.getTotalPrice());
    }

    public SalesOrder save(SalesOrder salesOrder) {
        if (salesOrder == null) {
            throw new IllegalArgumentException("Keine Bestellung zum Speichern vorhanden.");
        }
        if (salesOrder.getCustomer() == null) {
            throw new IllegalArgumentException("Für die Bestellung ist kein Kunde gesetzt.");
        }
        if (salesOrder.getPositions().isEmpty()) {
            throw new IllegalArgumentException("Bitte mindestens ein Produkt hinzufügen.");
        }
        mergeDuplicatePositions(salesOrder);
        salesOrder.setTotalCost(salesOrder.getTotalPrice());
        return orderRepository.save(salesOrder);
    }

    public boolean updateStatus(UUID orderId, String orderStatus, String shippingStatus) {
        return orderRepository.updateStatus(orderId, orderStatus, shippingStatus);
    }

    private void mergeDuplicatePositions(SalesOrder salesOrder) {
        Map<UUID, OrderPosition> positionByProductId = new LinkedHashMap<>();
        Iterator<OrderPosition> iterator = salesOrder.getPositions().iterator();

        while (iterator.hasNext()) {
            OrderPosition position = iterator.next();
            Product product = position.getProduct();
            if (product == null || product.getId() == null) {
                throw new IllegalArgumentException("Eine Position enthält kein gültiges Produkt.");
            }
            if (position.getQuantity() == null || position.getQuantity() < 1) {
                throw new IllegalArgumentException("Die Menge muss mindestens 1 sein.");
            }

            OrderPosition existing = positionByProductId.get(product.getId());
            if (existing == null) {
                positionByProductId.put(product.getId(), position);
                continue;
            }

            existing.setQuantity(existing.getQuantity() + position.getQuantity());
            iterator.remove();
        }
    }

}
