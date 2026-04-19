package de.pdbm.team18.business.stores.repository;

import de.pdbm.team18.business.stores.entity.Store;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class StoreRepository {

    @PersistenceContext
    EntityManager em;

    public List<Store> findAll() {
        return em.createQuery("SELECT s FROM Store s ORDER BY s.storeId", Store.class)
                .getResultList();
    }

    public int count() {
        return Math.toIntExact(em.createQuery("SELECT COUNT(s) FROM Store s", Long.class).getSingleResult());
    }
}
