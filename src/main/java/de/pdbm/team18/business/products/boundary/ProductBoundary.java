package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductControl;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named
@RequestScoped
public class ProductBoundary {

    //TODO Delete after implementation of data sources
    private final ArrayList<Product> dummyData = new ArrayList<>(List.of(new Product[]{
            new Product("TestProdukt1", "1234", new BigDecimal("0.12")),
            new Product("Test Produkt 2", "123456789", new BigDecimal("123.40"))
    }));
    private int number_of_products = dummyData.size();

    @Inject
    ProductControl control;

    public int getCount() {
	return number_of_products;
    }

    //TODO Replace with proper implementation
    public Product getProduct(UUID id) {
        for (Product p : this.dummyData) if (p.getId().equals(id)) return p;
        return null;
    }

    //TODO Replace with proper implementation
    public List<Product> getAllProducts() {
        return dummyData;
    }

    //TODO Replace with proper implementation
    public void addProduct(String productName, String productNumber, BigDecimal price) {
        this.dummyData.add(new Product(productName, productNumber, price));
    }

}
