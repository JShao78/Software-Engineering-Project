package de.pdbm.team18.business.orders.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

import de.pdbm.team18.business.orders.entity.*;
@Stateless
public class OrderControl {
    @PersistenceContext
    private EntityManager em;

    public List<SalesOrder>  getAllOrders(){
        return em.createQuery("SELECT o FROM SalesOrder o", SalesOrder.class).getResultList();
    }

    public SalesOrder getOrderById(UUID id){
        return em.find(SalesOrder.class, id);
    }

    //public void addOrder(UUID customerId){}
}
