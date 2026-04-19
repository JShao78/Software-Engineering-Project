package de.pdbm.team18.business.stores.boundary;

import de.pdbm.team18.business.stores.control.StoreController;
import de.pdbm.team18.business.stores.entity.Store;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class StoreBoundary implements Serializable {
    @Inject
    StoreController storeController;

    public List<Store> findAll() {
        return storeController.findAll();
    }

    public int count() {
        return storeController.count();
    }
}
