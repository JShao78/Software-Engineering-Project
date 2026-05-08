package de.pdbm.team18.business.stores.control;

import de.pdbm.team18.business.stores.entity.Staff;
import de.pdbm.team18.business.stores.repository.StaffRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
@RolesAllowed({"ADMIN"})
public class StaffController {

    @Inject
    private StaffRepository staffRepository;

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    /**
     * Provides the total number of entities.
     * @return the total number of entities
     */
    public int count() {
        return staffRepository.count();
    }


    public Staff create(Staff staff) {
        staff.setActive(1);
        return staffRepository.create(staff);
    }

    public boolean edit(Staff staff) {
        return staffRepository.update(staff) != null;
    }
}
