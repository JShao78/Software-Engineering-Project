package de.pdbm.team18.business.stores.boundary;
import de.pdbm.team18.business.shared.repository.RepositoryQueryUtils;
import de.pdbm.team18.business.stores.control.StockController;
import de.pdbm.team18.business.stores.entity.Stock;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.event.AjaxBehaviorEvent;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class StockBoundary implements Serializable {
    private int currentPage = 1;
    private int pageSize = RepositoryQueryUtils.DEFAULT_LIMIT;
    private int totalPages;

    private Stock editStock = new Stock();
    @Inject
    StockController stockController;

    public List<Stock> findPage() {
        return stockController.findPage(currentPage, pageSize);
    }

    public int totalPages() {
        return totalPages;
    }

    public int count() {
        int c = stockController.count();
        totalPages = Math.ceilDivExact(c, pageSize);
        return c;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.clamp(currentPage, 1, Math.max(totalPages(), 1));
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

    public boolean prepareEdit(Stock stockToEdit) {
        if (stockToEdit == null || stockToEdit.getStockId() == null) {
            cancelEdit();
            return false;
        }

        this.editStock = stockToEdit;
        return true;
    }

    public boolean saveEdit() {
        if (editStock.getStockId() == null) return false;
        boolean success = stockController.edit(
            editStock.getStockId(), 
            editStock.getQuantity()
        );
        if (success) cancelEdit();
        return success;
    }

    public void cancelEdit() {
        editStock = new Stock();
    }

    public Stock getEditStock() { return editStock; }
}
