package de.pdbm.team18.business.products.boundary;

import de.pdbm.team18.business.products.control.ProductController;
import de.pdbm.team18.business.products.entity.Brand;
import de.pdbm.team18.business.products.entity.Category;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ProductBoundary implements Serializable {

    private static final long serialVersionUID = 1L;

    private int currentPage;
    private int pageSize;

    private Product product;

    private Integer brandId;
    private Integer categoryId;

    private Product editProduct;

    private Integer editBrandId;
    private Integer editCategoryId;

    private boolean editModalOpen;

    @Inject
    private ProductController productController;

    @PostConstruct
    private void init() {
        currentPage = 1;
        pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;

        prepareNewProduct();
        prepareEmptyEditProduct();

        editModalOpen = false;
    }

    private void prepareNewProduct() {
        product = new Product();
        product.setBrand(new Brand());
        product.setCategory(new Category());

        brandId = null;
        categoryId = null;
    }

    private void prepareEmptyEditProduct() {
        editProduct = new Product();
        editProduct.setBrand(new Brand());
        editProduct.setCategory(new Category());

        editBrandId = null;
        editCategoryId = null;
    }

    public List<Product> findPage() {
        return productController.findPage(currentPage, pageSize);
    }

    public int totalPages() {
        return productController.totalPages(pageSize);
    }

    public int count() {
        return productController.count();
    }

    /*
     * FRONTEND-ONLY SAVE
     * This does NOT save the product to the database.
     * It only shows validation + success message and resets the form.
     */
    public String save() {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Frontend-Demo: Produkt wurde geprüft, aber nicht in der Datenbank gespeichert.",
                        null
                )
        );

        prepareNewProduct();

        return null;
    }

    /*
     * Opens the edit modal and fills it with the selected product data.
     */
    public void prepareEdit(Product selectedProduct) {
        this.editProduct = selectedProduct;

        if (selectedProduct.getBrand() != null) {
            this.editBrandId = selectedProduct.getBrand().getBrandId();
        } else {
            this.editBrandId = null;
        }

        if (selectedProduct.getCategory() != null) {
            this.editCategoryId = selectedProduct.getCategory().getCategoryId();
        } else {
            this.editCategoryId = null;
        }

        this.editModalOpen = true;
    }

    /*
     * Closes the edit modal without saving.
     */
    public void cancelEdit() {
        prepareEmptyEditProduct();
        this.editModalOpen = false;
    }

    /*
     * FRONTEND-ONLY UPDATE
     * This does NOT call productController.update().
     * So Maven will compile without needing backend update logic.
     */
    public String update() {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Frontend-Demo: Änderungen wurden nur in der Oberfläche übernommen.",
                        null
                )
        );

        prepareEmptyEditProduct();
        editModalOpen = false;

        return null;
    }

    public void onPageSizeChange(AjaxBehaviorEvent event) {
        this.pageSize = Math.clamp(pageSize, 1, RepositoryQueryUtils.MAX_LIMIT);
        this.currentPage = 1;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Product getEditProduct() {
        return editProduct;
    }

    public void setEditProduct(Product editProduct) {
        this.editProduct = editProduct;
    }

    public Integer getEditBrandId() {
        return editBrandId;
    }

    public void setEditBrandId(Integer editBrandId) {
        this.editBrandId = editBrandId;
    }

    public Integer getEditCategoryId() {
        return editCategoryId;
    }

    public void setEditCategoryId(Integer editCategoryId) {
        this.editCategoryId = editCategoryId;
    }

    public boolean isEditModalOpen() {
        return editModalOpen;
    }

    public void setEditModalOpen(boolean editModalOpen) {
        this.editModalOpen = editModalOpen;
    }
}