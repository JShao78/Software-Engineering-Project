package de.pdbm.team18.business.orders.boundary;

import de.pdbm.team18.business.orders.control.OrderController;
import de.pdbm.team18.business.orders.entity.Order;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class OrderBoundary implements Serializable {
    @Inject
    OrderController orderController;

    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
    private int currentPage = 1;

    public List<Order> findPage() {
        return orderController.findPage(currentPage, pageSize);
    }


    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return orderController.getTotalPages(pageSize);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.currentPage = 1;
    }

    public void nextPage() {
        if (this.currentPage < this.getTotalPages()) {
            this.currentPage++;
        }
    }
    public void previousPage() {
        if (this.currentPage > 1) {
            this.currentPage--;
        }
    }

}
