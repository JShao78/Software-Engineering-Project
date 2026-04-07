package de.pdbm.team18.business.stores.repository;

import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import de.pdbm.team18.business.stores.entity.Stock;
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
                        JOIN FETCH stock.store store
                        JOIN FETCH stock.product product
                        ORDER BY store.storeId, product.productId
                        """, Stock.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int count() {
        return Math.toIntExact(em.createQuery("SELECT COUNT(stock) FROM Stock stock", Long.class).getSingleResult());
    }
}
