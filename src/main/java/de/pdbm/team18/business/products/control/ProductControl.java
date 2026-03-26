package de.pdbm.team18.business.products.control;
import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.products.repository.ProductRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
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

    public boolean updateStock(UUID productId, int newStock){
        return productRepository.updateStock(productId, newStock);
    }


}
