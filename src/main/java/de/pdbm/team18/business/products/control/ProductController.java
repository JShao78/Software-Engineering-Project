package de.pdbm.team18.business.products.control;

import de.pdbm.team18.business.products.entity.Brand;
import de.pdbm.team18.business.products.entity.Category;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.products.repository.ProductRepository;
import de.pdbm.team18.business.stores.control.StockController;
import de.pdbm.team18.business.stores.control.StoreController;
import de.pdbm.team18.business.stores.entity.Stock;
import de.pdbm.team18.business.stores.entity.StockId;
import de.pdbm.team18.business.stores.entity.Store;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
@RolesAllowed({"ADMIN", "USER1", "USER2"})
public class ProductController {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private StockController stockController;

    @Inject
    private StoreController storeController;

    public List<Product> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return productRepository.findPage(offset, pageSize);
    }

    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    public int totalPages(int pageSize) {
        int count = productRepository.count();
        if (count == 0) return 1;
        return (int) Math.ceil((double) count / pageSize);
    }

    public int count() {
        return productRepository.count();
    }
    @RolesAllowed("ADMIN")
    public Product create(Product product) {
        productRepository.create(product);
        for (Store store : storeController.findAll()) {
            Stock stock = new Stock();
            stock.setProduct(product);
            stock.setQuantity(0);
            stock.setStore(store);
            stock.setStockId(new StockId());
            stockController.create(stock);
        }
        return product;
    }

    public Product findById(Integer productId) {
        return productRepository.findById(productId);
    }

    @RolesAllowed({"ADMIN", "USER1"})
    public boolean edit(Product product) {
        return productRepository.update(product) != null;
    }

    public List<Category> findCategories() {
        return productRepository.findCategories();
    }

    public List<Brand> findBrands() {
        return productRepository.findBrands();
    }
}