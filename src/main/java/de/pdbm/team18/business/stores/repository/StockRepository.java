package de.pdbm.team18.business.stores.repository;

import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import de.pdbm.team18.business.stores.entity.Stock;
import de.pdbm.team18.business.stores.entity.StockId;
import de.pdbm.team18.business.stores.entity.Store;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class StockRepository {

    @PersistenceContext
    EntityManager em;

    public List<Stock> findPage(int offset, int limit) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);

        return em.createQuery("""
                    SELECT stock
                    FROM Stock stock
                    JOIN FETCH stock.store
                    JOIN FETCH stock.product
                    ORDER BY stock.store.storeId, stock.product.productId
                    """, Stock.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int count() {
        return Math.toIntExact(em.createQuery("SELECT COUNT(stock) FROM Stock stock", Long.class).getSingleResult());
    }

    public Stock create(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("stock must not be null");
        }

        em.persist(stock);
        em.flush();
        return stock;
    }

    public Stock updateQuantity(StockId stockId, int quantity) {
        Stock managed = em.find(Stock.class, stockId);
        if (managed == null) return null;
        managed.setQuantity(quantity);
        em.flush();
        return managed;
    }

    public Integer getQuantity(Store store, Product product) {
        if (store == null || product == null) return null;
        return em.createQuery("SELECT stock.quantity FROM Stock stock WHERE stock.store = :store AND stock.product = :product", Integer.class)
                .setParameter("store", store)
                .setParameter("product", product)
                .getSingleResult();
    }
}
