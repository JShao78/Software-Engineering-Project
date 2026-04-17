package de.pdbm.team18.business.products.control;

import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.products.repository.ProductRepository;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class ProductController {

    @Inject
    private ProductRepository productRepository;

    public List<Product> findPage(int pageNumber) {
        pageNumber = Math.max(pageNumber, 1);
        int offset = (pageNumber - 1) * RepositoryQueryUtils.DEFAULT_LIMIT;
        return productRepository.findPage(offset, 0);
    }

    public int totalPages() {
        return Math.ceilDivExact(productRepository.count(), RepositoryQueryUtils.DEFAULT_LIMIT);
    }
}
