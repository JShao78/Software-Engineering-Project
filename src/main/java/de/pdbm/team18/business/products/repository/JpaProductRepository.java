package de.pdbm.team18.business.products.repository;

import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Stateless
public class JpaProductRepository implements ProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findPage(String searchTerm, int offset, int limit) {
        boolean hasSearch = RepositoryQueryUtils.hasText(searchTerm);
        String searchPattern = hasSearch ? RepositoryQueryUtils.toLikePattern(searchTerm) : "%";

        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p " +
                "WHERE (:hasSearch = false " +
                "   OR LOWER(p.productName) LIKE :searchPattern " +
                "   OR LOWER(p.productNumber) LIKE :searchPattern) " +
                "ORDER BY p.productName ASC, p.id ASC",
            Product.class
        );

        return query
            .setParameter("hasSearch", hasSearch)
            .setParameter("searchPattern", searchPattern)
            .setFirstResult(RepositoryQueryUtils.sanitizeOffset(offset))
            .setMaxResults(RepositoryQueryUtils.sanitizeLimit(limit))
            .getResultList();
    }

    @Override
    public long count(String searchTerm) {
        boolean hasSearch = RepositoryQueryUtils.hasText(searchTerm);
        String searchPattern = hasSearch ? RepositoryQueryUtils.toLikePattern(searchTerm) : "%";

        return entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p " +
                    "WHERE (:hasSearch = false " +
                    "   OR LOWER(p.productName) LIKE :searchPattern " +
                    "   OR LOWER(p.productNumber) LIKE :searchPattern)",
                Long.class
            )
            .setParameter("hasSearch", hasSearch)
            .setParameter("searchPattern", searchPattern)
            .getSingleResult();
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(Product.class, id));
    }

    @Override
    public List<Product> findByIds(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id ASC",
                Product.class
            )
            .setParameter("ids", ids)
            .getResultList();
    }

    @Override
    public Optional<Product> findByProductNumber(String productNumber) {
        if (!RepositoryQueryUtils.hasText(productNumber)) {
            return Optional.empty();
        }

        return entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.productNumber = :productNumber",
                Product.class
            )
            .setParameter("productNumber", productNumber.trim())
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() != null) {
            return entityManager.merge(product);
        }
        entityManager.persist(product);
        return product;
    }

    @Override
    public boolean updateStock(UUID productId, int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("newStock must be >= 0");
        }

        Product product = entityManager.find(Product.class, productId);
        if (product == null) {
            return false;
        }

        product.setStock(newStock);
        return true;
    }
}
