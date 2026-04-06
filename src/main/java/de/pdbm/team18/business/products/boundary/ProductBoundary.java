package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
public class ProductBoundary {

    private int currentPage = 1;

    @Inject
    ProductController productController;

    public List<Product> findPage() {
        return productController.findPage(currentPage);
    }

    public int totalPages() {
        return productController.totalPages();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(currentPage, 1);
    }
}
