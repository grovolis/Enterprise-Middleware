package org.jboss.quickstarts.wfk.customer;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;


public class CustomerRepository {
    @Inject
    private EntityManager em;
    
    List<Customer> findAllOrderedByName() { //returns all customer objects sorted by last name
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_ALL, Customer.class);
        return query.getResultList();
    }
    
    Customer findByEmail(String email) throws NonUniqueResultException { //returns customer that uses the requested email
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_BY_EMAIL, Customer.class).setParameter("email", email);
        Customer result = null; //if email not found then result is null
        
        try{ 
        	result = query.getSingleResult();
        } catch (NoResultException e) {}
        
        return result;
    }
    
    Customer findById(Long id) { //returns customer by ID number
    	return em.find(Customer.class, id);
    }
    
    Customer create(Customer customer) throws EntityExistsException {
        // stores customer to the Database
        em.persist(customer);

        return customer;
    }
    
    public Customer update(Customer customer) {
    	// if the customer is already stores in the database it gets updated otherwise it is just added
        em.merge(customer);
        
        return customer;
    }
    
    Customer delete(Customer customer) { //deletes the customer from the database
    	
        if (customer.getId() != null) { //customer id cannot be null
        	try {
        		em.remove(em.merge(customer)); //the object is merged upon removal in order to become persistent first otherwise i cannot be deleted
        	} catch (IllegalArgumentException e) {
        		return null;
        	}
        }

        return customer;
    }
}
