package de.pdbm.team18.business.products.repository;

import de.pdbm.team18.business.products.entity.Brand;
import de.pdbm.team18.business.products.entity.Category;
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
                SELECT p FROM Product p
                LEFT JOIN FETCH p.brand
                LEFT JOIN FETCH p.category
                ORDER BY p.productId
                """, Product.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Product> findByName(String name) {
        if (name == null || name.length() < 3) return List.of();
        name = name.toLowerCase();
        return em.createQuery("""
                        SELECT p
                        FROM Product p
                        WHERE lower(p.productName) LIKE CONCAT('%', :name, '%')
                        ORDER BY p.productName
                        """, Product.class)
                .setParameter("name", name)
                .setMaxResults(RepositoryQueryUtils.SEARCH_QUERY_LIMIT)
                .getResultList();
    }

    public int count() {
        return Math.toIntExact(
                em.createQuery("SELECT COUNT(p) FROM Product p", Long.class)
                        .getSingleResult());
    }

    public Product create(Product product) {
        if (product == null) throw new IllegalArgumentException("Product must not be null");
        em.persist(product);
        em.flush();
        return product;
    }

    public Product update(Product product) {
        if (product == null) throw new IllegalArgumentException("Product must not be null");
        Product managed = em.merge(product);
        em.flush();
        return managed;
    }

    public List<Brand> findBrands() {
        return em.createQuery("SELECT b FROM Brand b", Brand.class).getResultList();
    }

    public List<Category> findCategories() {
        return em.createQuery("SELECT c FROM Category c", Category.class).getResultList();
    }
}
