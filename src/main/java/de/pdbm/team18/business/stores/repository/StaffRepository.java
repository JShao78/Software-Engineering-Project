package de.pdbm.team18.business.stores.repository;

import de.pdbm.team18.business.stores.entity.Staff;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class StaffRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Staff> findAll() {
        return em.createQuery("""
            SELECT s
            FROM Staff s
            LEFT JOIN FETCH s.storeId
            LEFT JOIN FETCH s.managerId
            ORDER BY s.staffId
            """, Staff.class)
                .getResultList();
    }

    public List<Staff> findIdFirstAndLastName() {
        List<Object[]> rows = em.createQuery("""
                SELECT s.staffId, s.firstName, s.lastName
                FROM Staff s
                ORDER BY s.staffId
                """, Object[].class)
                .getResultList();

        return rows.stream()
                .map(row -> {
                    Staff staff = new Staff();
                    staff.setStaffId((Integer) row[0]);
                    staff.setFirstName((String) row[1]);
                    staff.setLastName((String) row[2]);
                    return staff;
                })
                .toList();
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
