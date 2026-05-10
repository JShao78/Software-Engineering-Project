package de.pdbm.team18.business.stores.control;

import de.pdbm.team18.business.stores.entity.Staff;
import de.pdbm.team18.business.stores.repository.StaffRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
@RolesAllowed({"ADMIN", "USER1"})
public class StaffController {

    @Inject
    private StaffRepository staffRepository;

    @RolesAllowed("ADMIN")
    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    /**
     * Provides the total number of entities.
     * @return the total number of entities
     */
    @RolesAllowed("ADMIN")
    public int count() {
        return staffRepository.count();
    }


    @RolesAllowed("ADMIN")
    public Staff create(Staff staff) {
        staff.setActive(1);
        return staffRepository.create(staff);
    }

    @RolesAllowed("ADMIN")
    public boolean edit(Staff staff) {
        return staffRepository.update(staff) != null;
    }

    @RolesAllowed({"ADMIN", "USER1"})
    public List<Staff> findForOrderSelection() {
        return staffRepository.findIdFirstAndLastName();
    }
}
