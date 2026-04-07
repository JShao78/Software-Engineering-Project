package de.pdbm.team18.business.products.control;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.products.repository.ProductRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Stateless
public class ProductControl {
    @Inject
    private ProductRepository productRepository;

    public List<Product> findPage(String searchTerm, int offset, int limit){
        return productRepository.findPage(searchTerm, offset, limit);
    }

    public long count(String searchTerm){
        return productRepository.count(searchTerm);
    }

    public Product findById(UUID id){
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> findByIds(Set<UUID> ids){
        return productRepository.findByIds(ids);
    }

    public Product findByProductNumber(String productNumber){
        return productRepository.findByProductNumber(productNumber).orElse(null);
    }

    public Product saveProduct(Product product){
        return productRepository.save(product);
    }

    public Product createProduct(String productName, BigDecimal price) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Der Produktname darf nicht leer sein.");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Der Preis muss >= 0 sein.");
        }

        Product product = new Product(productName.trim(), generateUniqueProductNumber(), price);
        return productRepository.save(product);
    }

    public boolean updateStock(UUID productId, int newStock){
        return productRepository.updateStock(productId, newStock);
    }

    private String generateUniqueProductNumber() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "P-" + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);
            if (productRepository.findByProductNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Es konnte keine eindeutige Produktnummer erzeugt werden.");
    }


}
