package de.pdbm.team18.business.stores.control;

import de.pdbm.team18.business.stores.entity.Store;
import de.pdbm.team18.business.stores.repository.StoreRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
public class StoreController {

    @Inject
    private StoreRepository storeRepository;

    public List<Store> findPage(int currentPage, int pageSize) {
        int offset = (currentPage - 1) * pageSize;
        return storeRepository.findPage(offset, pageSize);
    }

    public int totalPages(int pageSize) {
        return Math.ceilDivExact(storeRepository.count(), pageSize);
    }

}
