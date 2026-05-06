package de.pdbm.team18.business.stores.repository;

import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import de.pdbm.team18.business.stores.entity.Staff;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class StaffRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Provies a List excerpt of the table starting at offset and including limit elements.
     * @param offset the index of the first entry
     * @param limit the total number of entries to return
     * @return the List excerpt
     */
    public List<Staff> findPage(int offset, int limit) {
        offset = RepositoryQueryUtils.sanitizeOffset(offset);
        limit = RepositoryQueryUtils.sanitizeLimit(limit);

        return em.createQuery("""
            SELECT s
            FROM Staff s
            LEFT JOIN FETCH s.storeId
            LEFT JOIN FETCH s.managerId
            ORDER BY s.staffId
            """, Staff.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * @return the total number of entries in the table
     */
    public int count() {
        return Math.toIntExact(em.createQuery("SELECT COUNT(s) FROM Staff s", Long.class).getSingleResult());
    }

    /**
     * Persist the given Staff-entity.
     * @param staff the entity to persist
     * @return a reference back to the entity-Object
     */
    public Staff create(Staff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff must not be null");
        }

        em.persist(staff);
        em.flush();
        return staff;
    }

    public Staff update(Staff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff must not be null");
        }

        Staff managedStaff = em.merge(staff);
        em.flush();
        return managedStaff;
    }
}
