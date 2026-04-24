package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Brand;
import de.pdbm.team18.business.products.entity.Category;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.event.AjaxBehaviorEvent;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ProductBoundary implements Serializable {

    private int currentPage;
    private int pageSize;

    private Product product;

    /**
     * Initializes parameters to ensure no old temporary Product us used between injections.
     */
    @PostConstruct
    private void init() {
        currentPage = 1;
        pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
        product = new Product();
        product.setBrand(new Brand());
        product.setCategory(new Category());
    }

    @Inject
    ProductController productController;

    public List<Product> findPage() {
        return productController.findPage(currentPage, pageSize);
    }

    public int totalPages() {
        return productController.totalPages(pageSize);
    }

    public int count() {
        return productController.count();
    }

    /**
     * Persists the currently stored, temporary Product and creates a new one.
     */
    public void save() {
        productController.create(product);
        product = new Product();
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
    }

    public Product getProduct() {
        return product;
    }
}
