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

    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    public int count() {
        return storeRepository.count();
    }

}
