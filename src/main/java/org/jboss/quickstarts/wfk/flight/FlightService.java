package org.jboss.quickstarts.wfk.flight;

import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

public class FlightService {
	@Inject //call retrieve update delete dependency
	FlightRepository crud;
	
	@Inject //validator dependency
	FlightValidator validator;
	
    public List<Flight> findAll() { //returns all flight objects sorted by flight number
        return crud.findAll();
    }
    

    public Flight findById(Long id) { //returns the flight that matches the ID
    	return crud.findById(id);
    }
    
    public Flight create(Flight flight) throws ConstraintViolationException, ValidationException, Exception {
    	//Validates the the data to be passed to the flight entity match the parameters needed
    	validator.validate(flight);
    	
        //stores the flight to the database
        return crud.create(flight);
    }
    
    public Flight delete(Flight flight) throws Exception {
        return crud.delete(flight); //deletes flight from the database if it exists
    }
}
