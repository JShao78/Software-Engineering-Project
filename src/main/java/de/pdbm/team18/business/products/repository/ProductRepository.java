package de.pdbm.team18.business.products.repository;

import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class ProductRepository {

    @PersistenceContext
    EntityManager em;

    public List<Product> findPage(int offset, int limit) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);

        return em.createQuery("""
                        SELECT p
                        FROM Product p
                        LEFT JOIN FETCH p.brand
                        LEFT JOIN FETCH p.category
                        ORDER BY p.productId
                        """, Product.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
