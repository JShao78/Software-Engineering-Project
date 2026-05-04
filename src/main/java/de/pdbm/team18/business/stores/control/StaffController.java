package de.pdbm.team18.business.stores.control;

import de.pdbm.team18.business.stores.entity.Staff;
import de.pdbm.team18.business.stores.repository.StaffRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
@RolesAllowed({"ADMIN", "USER1", "USER2"})
public class StaffController {

    @Inject
    private StaffRepository staffRepository;

    /**
     * Provides a List representing the given page of the given size.
     * @param pageNumber the number of the page
     * @param pageSize the size of the page
     * @return a List representing the page
     */
    public List<Staff> findPage(int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        return staffRepository.findPage(offset, pageSize);
    }

    /**
     * Determines the total number of pages for a given page size.
     * @param pageSize the size of the pages
     * @return the number of pages
     */
    public int totalPages(int pageSize) {
        return Math.ceilDivExact(staffRepository.count(), pageSize);
    }

    /**
     * Provides the total number of entities.
     * @return the total number of entities
     */
    public int count() {
        return staffRepository.count();
    }

    @RolesAllowed( "ADMIN")
    public Staff create(Staff staff) {
        staff.setActive(1);
        return staffRepository.create(staff);
    }

    public Staff findById(Integer staffId) {
        return staffRepository.findById(staffId);
    }
    @RolesAllowed({"ADMIN", "USER1"})
    public boolean edit(Staff staff) {
        return staffRepository.update(staff) != null;
    }
}
