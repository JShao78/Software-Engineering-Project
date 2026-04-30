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
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
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
        return Math.ceilDivExact(productRepository.count(), pageSize);
    }

    public int count() {
        return productRepository.count();
    }

    /**
     * Persist the given Product and creates Stock entries for all Stores
     * @param product the Product to persist
     * @return the same Product
     */
    public Product create(Product product) {
        productRepository.create(product);
        // Create Stocks for each store
        for (Store store: storeController.findAll()) {
            Stock stock = new Stock();
            stock.setProduct(product);
            stock.setQuantity(0);
            stock.setStore(store);
            stock.setStockId(new StockId()); // TODO double check if required
            stockController.create(stock);
        }
        return product;
    }

    public List<Category> findCategories() {
        return productRepository.findCategories();
    }

    public List<Brand>  findBrands() {
        return productRepository.findBrands();
    }
}
