package de.pdbm.team18.business.stores.boundary;

import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
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

    private int currentPage = 1;
    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;

    @Inject
    StaffController staffController;

    private Staff staff = new Staff();

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
}
