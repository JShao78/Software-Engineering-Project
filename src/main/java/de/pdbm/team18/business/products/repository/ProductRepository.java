package de.pdbm.team18.business.products.repository;

import de.pdbm.team18.business.products.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProductRepository {
    List<Product> findPage(String searchTerm, int offset, int limit);

    long count(String searchTerm);

    Optional<Product> findById(UUID id);

    List<Product> findByIds(Set<UUID> ids);

    Optional<Product> findByProductNumber(String productNumber);

    Product save(Product product);

    boolean updateStock(UUID productId, int newStock);
}
