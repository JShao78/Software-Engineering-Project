package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
public class ProductBoundary {

    @Inject
    ProductController productController;

    public List<Product> findPage(int offset, int limit) {
        return productController.findPage(offset, limit);
    }
}
