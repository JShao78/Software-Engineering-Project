package de.pdbm.team18.business.stores.boundary;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import de.pdbm.team18.business.stores.control.StockController;
import de.pdbm.team18.business.stores.entity.Stock;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class StockBoundary implements Serializable {
    private int currentPage = 1;
    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
    @Inject
    StockController stockController;

    public List<Stock> findPage() {
        return stockController.findPage(currentPage, pageSize);
    }

    public int totalPages() {
        return stockController.getTotalPages(pageSize);
    }



    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.clamp(currentPage, 1, Math.max(totalPages(), 1));
    }
}
