package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ProductBoundary implements Serializable {

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
