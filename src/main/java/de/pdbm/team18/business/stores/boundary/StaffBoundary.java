package de.pdbm.team18.business.stores.boundary;

import de.pdbm.team18.business.stores.control.StaffController;
import de.pdbm.team18.business.stores.control.StoreController;
import de.pdbm.team18.business.stores.entity.Staff;
import de.pdbm.team18.business.stores.entity.Store;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class StaffBoundary implements Serializable {
    @Inject
    private StaffController staffController;

    @Inject
    private StoreController storeController;

    private Staff staff = new Staff();
    private Integer storeId;

    private Staff editStaff = new Staff();
    private Integer editStoreId;

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
        staff.setStoreId(findStoreById(storeId));
        staffController.create(staff);
        staff = new Staff();
        storeId = null;
    }

    public boolean prepareEdit(Staff staffToEdit) {
        if (staffToEdit == null || staffToEdit.getStaffId() <= 0) {
            cancelEdit();
            return false;
        }

        this.editStaff = staffToEdit;
        this.editStoreId = staffToEdit.getStoreId() != null
                ? staffToEdit.getStoreId().getStoreId()
                : null;

        return true;
    }

    public boolean saveEdit() {
        if (editStaff.getStaffId() <= 0) {
            return false;
        }

        editStaff.setStoreId(findStoreById(editStoreId));

        boolean success = staffController.edit(editStaff);
        if (success) {
            cancelEdit();
        }
        return success;
    }

    public void cancelEdit() {
        editStaff = new Staff();
        editStoreId = null;
    }

    private Store findStoreById(Integer id) {
        if (id == null) {
            return null;
        }

        return storeController.findAll()
                .stream()
                .filter(store -> store.getStoreId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Staff getStaff() {
        return staff;
    }

    public Staff getEditStaff() {
        return editStaff;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Integer getEditStoreId() {
        return editStoreId;
    }

    public void setEditStoreId(Integer editStoreId) {
        this.editStoreId = editStoreId;
    }
}