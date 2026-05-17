package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Brand;
import de.pdbm.team18.business.products.entity.Category;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ProductBoundary implements Serializable {

    private int currentPage = 1;
    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
    private int totalPages;

    private Product product = new Product();
    private Integer brandId;
    private Integer categoryId;

    private Product editProduct = new Product();
    private Integer editBrandId;
    private Integer editCategoryId;

    @Inject
    private ProductController productController;

    public List<Product> findPage() {
        return productController.findPage(currentPage, pageSize);
    }

    public int totalPages() {
        return totalPages;
    }

    public int count() {
        int c = productController.count();
        totalPages = Math.ceilDivExact(c, pageSize);
        return c;
    }

    public void create() {
        product.setBrand(productController.findBrands()
                .stream().filter(b -> b.getBrandId().equals(brandId))
                .findFirst().orElse(null));
        product.setCategory(productController.findCategories()
                .stream().filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst().orElse(null));
        productController.create(product);
        product = new Product();
        brandId = null;
        categoryId = null;
    }

    public boolean prepareEdit(Product productToEdit) {
        if (productToEdit == null || productToEdit.getProductId() == null) {
            cancelEdit();
            return false;
        }

        this.editProduct = productToEdit;
        this.editBrandId = productToEdit.getBrand() != null ? productToEdit.getBrand().getBrandId() : null;
        this.editCategoryId = productToEdit.getCategory() != null ? productToEdit.getCategory().getCategoryId() : null;
        return true;
    }

    public boolean saveEdit() {
        if (editProduct.getProductId() == null) return false;

        editProduct.setBrand(productController.findBrands()
                .stream().filter(b -> b.getBrandId().equals(editBrandId))
                .findFirst().orElse(null));
        editProduct.setCategory(productController.findCategories()
                .stream().filter(c -> c.getCategoryId().equals(editCategoryId))
                .findFirst().orElse(null));

        boolean success = productController.edit(editProduct);
        if (success) cancelEdit();
        return success;
    }

    public void cancelEdit() {
        editProduct = new Product();
        editBrandId = null;
        editCategoryId = null;
    }

    public void onPageSizeChange(AjaxBehaviorEvent event) {
        this.pageSize = Math.clamp(pageSize, 1, RepositoryQueryUtils.MAX_LIMIT);
        this.currentPage = 1;
    }

    // Getters and Setters

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.clamp(currentPage, 1, Math.max(totalPages(), 1));
    }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getBrandId() { return brandId; }
    public void setBrandId(Integer brandId) { this.brandId = brandId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Product getEditProduct() { return editProduct; }

    public Integer getEditBrandId() { return editBrandId; }
    public void setEditBrandId(Integer editBrandId) { this.editBrandId = editBrandId; }

    public Integer getEditCategoryId() { return editCategoryId; }
    public void setEditCategoryId(Integer editCategoryId) { this.editCategoryId = editCategoryId; }

    public List<Brand> findBrands() { return productController.findBrands(); }
    public List<Category> findCategories() { return productController.findCategories(); }
}
