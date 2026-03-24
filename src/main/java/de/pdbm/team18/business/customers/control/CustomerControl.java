package de.pdbm.team18.business.customers.control;

import jakarta.ejb.Stateless;
import de.pdbm.team18.business.customers.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Stateless
public class CustomerControl {
    @PersistenceContext
    private EntityManager em;

    public List<Customer> getAllCustomers() {
        return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    }

    public Customer findCustomerById(UUID id) {
        return em.find(Customer.class, id);
    }

    public void addCustomer(Customer customer) {
        em.persist(customer);
    }

}
