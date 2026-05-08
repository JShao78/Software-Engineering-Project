package de.pdbm.team18.business.stores.boundary;

import de.pdbm.team18.business.stores.control.StaffController;
import de.pdbm.team18.business.stores.entity.Staff;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class StaffBoundary implements Serializable {
    @Inject
    StaffController staffController;

    private Staff staff = new Staff();
    private Staff editStaff = new Staff();

    public List<Staff> findAll() {
        return staffController.findAll();
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

    public boolean prepareEdit(Staff staffToEdit) {
        if (staffToEdit == null || staffToEdit.getStaffId() <= 0) {
            cancelEdit();
            return false;
        }

        this.editStaff = staffToEdit;
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

    public Staff getStaff() {
        return staff;
    }

    public Staff getEditStaff() {
        return editStaff;
    }
}
