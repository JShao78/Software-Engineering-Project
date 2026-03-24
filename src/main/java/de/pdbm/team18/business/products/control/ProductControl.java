package de.pdbm.team18.business.products.control;
import de.pdbm.team18.business.products.entity.Product;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ProductControl {
    @PersistenceContext
    private EntityManager em;

    public List<Product> getAllProducts(){
        return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    public void saveProduct(Product product){
        em.persist(product);
    }

    public Product findProductById(Long id){
        return em.find(Product.class, id);
    }
}
