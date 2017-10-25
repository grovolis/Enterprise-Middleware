package org.jboss.quickstarts.wfk.customer;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check Customer objects against arbitrary requirements.</p>
 */
public class CustomerValidator{
	@Inject //validator dependency
	private Validator validator;
	
    @Inject //automatic logging dependency
    private @Named("logger") Logger log;

    @Inject //calls crud dependency
    private CustomerRepository crud;
    
    private void commonValidate(Customer customer) throws ConstraintViolationException {
        //creates a bean to validate customer data.
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
    }
    
    void validate(Customer customer) throws ConstraintViolationException, CustomerExistsException {
    	commonValidate(customer);

        if (emailAlreadyExists(customer)) { //check for duplicate email address
            throw new CustomerExistsException("Customer's email already exists");
        }
    }
    
    void validateExisting(Customer customer) throws ConstraintViolationException, CustomerExistsException {
        commonValidate(customer);
        //if customer's email has changed, check the new ones existence in the database
        // if customer is already in the database, then check if the new email provided (if changed), doesn't exists in the database yet
        Customer fromDb = crud.findByEmail(customer.getEmail());
        
        //check for duplicate email
        if(fromDb != null && (!fromDb.getId().equals(customer.getId()))) {
        	throw new CustomerExistsException("Customer's email already exists");
        }
    }
    
    //checks if customer exists in the database based on their email address
    boolean emailAlreadyExists(Customer customer) {
    	if(customer == null) {
    		return false;
    	}
    	
    	Customer fromDb = null;
    	String email = customer.getEmail();

    	try{
    		fromDb = crud.findByEmail(email);
    		//if a customer matches the email address and the ID is the same
    		return fromDb != null && fromDb.equals(customer);
    	} catch (NonUniqueResultException e) {
    		log.warning("The email: " + email + " already exists in the database");
    		return true;
    	}
    }
}