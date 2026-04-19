package de.pdbm.team18.business.products.control;

import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.products.repository.ProductRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class ProductController {

    @Inject
    private ProductRepository productRepository;

    public List<Product> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return productRepository.findPage(offset, pageSize);
    }

    public int totalPages(int pageSize) {
        return Math.ceilDivExact(productRepository.count(), pageSize);
    }

    public int count() {
        return productRepository.count();
    }
}
