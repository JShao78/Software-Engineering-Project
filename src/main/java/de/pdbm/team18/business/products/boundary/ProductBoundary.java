package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductControl;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Named
@RequestScoped
public class ProductBoundary {

    //Properties
    private String productName;
    private BigDecimal price;

    public void setProductName(String productName) { this.productName = productName; }
    public String getProductName() { return this.productName; }

    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getPrice() { return this.price; }

    @Inject
    private ProductControl controller;

    public long getCount() {
	    return controller.count(null);
    }

    /**
     * Returns the Product with the provided UUID.
     * @param id the Product's UUID.
     * @return the Product or null if none exists.
     */
    public Product getProduct(UUID id) {
        return controller.findById(id);
    }

    /**
     * Returns a page of Products matching the search term. For more info see repository.
     * @param searchTerm the search term.
     * @param offset the page number.
     * @param limit the page size.
     * @return a list representing the page.
     */
    public List<Product> getAllProducts(String searchTerm, int offset, int limit) {
        return controller.findPage(searchTerm, offset, limit);
    }

    /**
     * Calls the parameterized getAllProducts()-method with default parameters.
     * @deprecated use parameterized method instead. TODO remove once unused.
     * @return the first page of up to 100 Products.
     */
    public List<Product> getAllProducts() {
        return getAllProducts(null, 0, 100);
    }

    /**
     * Sends a product to the controller to be stored persistently.
     */
    public void addProduct() {
        controller.saveProduct(new Product(productName, Long.toString(controller.count(null)), price));
        this.productName = null;
        this.price = null;
    }

}
