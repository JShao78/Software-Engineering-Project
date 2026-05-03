package de.pdbm.team18.business.stores.control;

import de.pdbm.team18.business.products.entity.Product;
import de.pdbm.team18.business.stores.entity.Stock;
import de.pdbm.team18.business.stores.entity.StockId;
import de.pdbm.team18.business.stores.entity.Store;
import de.pdbm.team18.business.stores.repository.StockRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
@RolesAllowed({"ADMIN", "USER1", "USER2"})
public class StockController {

    @Inject
    private StockRepository stockRepository;

    public List<Stock> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return stockRepository.findPage(offset, pageSize);
    }

    public int totalPages(int pageSize) {
        return Math.ceilDivExact(stockRepository.count(), pageSize);
    }

    public int count() {
        return stockRepository.count();
    }

    @RolesAllowed("ADMIN")
    public Stock create(Stock stock) {
        return stockRepository.create(stock);
    }

    @RolesAllowed({"ADMIN", "USER1"})
    public boolean updateQuantity(StockId stockId, int quantity) {
        return stockRepository.updateQuantity(stockId, quantity) != null;
    }

    public Stock findById(StockId stockId) {
        return stockRepository.findById(stockId);
    }

    public Integer getQuantity(Store store, Product product) {
        return stockRepository.getQuantity(store, product);
    }
}
