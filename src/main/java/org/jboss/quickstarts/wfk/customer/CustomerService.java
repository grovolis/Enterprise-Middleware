package org.jboss.quickstarts.wfk.customer;

import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

public class CustomerService {
	
	@Inject //validator dependency
	CustomerValidator validator;

	@Inject //call retrieve update delete dependency
	CustomerRepository crud;
	
    public List<Customer> findAllOrderedByName() { //returns all customer objects sorted by last name
        return crud.findAllOrderedByName();
    }
    
    public Customer findById(Long id) { //returns the customer that matches the ID
        return crud.findById(id);
    }
    
    public Customer create(Customer customer) throws ConstraintViolationException, CustomerExistsException { //linked to CustomerRepository
    	//Validates the the data to be passed to the customer entity match the parameters needed
        validator.validate(customer);

        //stores customer to the database
        return crud.create(customer);
    }
    
    public Customer update(Customer customer) throws ConstraintViolationException, CustomerExistsException { //updates customer to the database if it exists
    	//Validates the the data to be passed to the customer entity match the parameters needed
        validator.validateExisting(customer);
        //updates the customer's entry in the database
        return crud.update(customer);
    }
    
    public Customer delete(Customer customer) { 
        return crud.delete(customer); //deletes customer from the database if it exists
    }
}
