package de.pdbm.team18.business.stores.control;

import de.pdbm.team18.business.stores.entity.Stock;
import de.pdbm.team18.business.stores.repository.StockRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
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

    public Stock create(Stock stock) {
        return stockRepository.create(stock);
    }
}
