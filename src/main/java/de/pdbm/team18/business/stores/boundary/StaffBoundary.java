package de.pdbm.team18.business.stores.boundary;

import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import de.pdbm.team18.business.stores.repository.StaffRepository;
import de.pdbm.team18.business.stores.control.StaffController;
import de.pdbm.team18.business.stores.entity.Staff;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class StaffBoundary implements Serializable {
	private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private int status;

    //Getters and Setters for properties
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }

    public String getEmail() { return email; }
    public void setEmail(String email) {this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    private int currentPage = 1;
    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;

    @Inject
    StaffController staffController;
    @Inject
    StaffRepository staffRepository;

    private Staff staff = new Staff();
    private Staff editStaff = new Staff();

    /**
     * Provides a List of Staff representing the selected page.
     * @return The List of Staff
     */
    public List<Staff> findPage() {
        return staffController.findPage(currentPage, pageSize);
    }

    /**
     * @return The total number of pages for the current pageSize
     */
    public int totalPages() {
        return staffController.totalPages(pageSize);
    }

    /**
     * @return the total number of Entities
     */
    public int count() {
        return staffController.count();
    }

    public void create() {
        staffController.create(staff);
        staff = new Staff();
    }

    public boolean prepareEdit(Integer staffId) {
        Staff foundStaff = staffController.findById(staffId);
        if (foundStaff == null) {
            cancelEdit();
            return false;
        }
        this.editStaff = foundStaff;
        return true;
    }

    public boolean saveEdit() {
        if (editStaff.getStaffId() <= 0) {
            return false;
        }

        boolean success = staffController.edit(editStaff);
        if (success) {
            cancelEdit();
        }
        return success;
    }

    public void cancelEdit() {
        this.editStaff = new Staff();
    }

    public void addStaff() {
        Staff staff = new Staff();
        staff.setFirstName(firstName);
        staff.setLastName(lastName);
        staff.setEmail(email);
        staff.setPhone(phone);
        staff.setActive(status);

        staffRepository.create(staff);

        this.firstName = null;
        this.lastName = null;
        this.email = null;
        this.phone = null;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.clamp(currentPage, 1, Math.max(totalPages(), 1)); // fixed: was pageSize
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void onPageSizeChange(AjaxBehaviorEvent event) {
        this.pageSize = Math.clamp(pageSize, 1, RepositoryQueryUtils.MAX_LIMIT);
        this.currentPage = 1;
    }

    public Staff getStaff() {
        return staff;
    }

    public Staff getEditStaff() {
        return editStaff;
    }
}
