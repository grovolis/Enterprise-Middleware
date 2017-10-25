package org.jboss.quickstarts.wfk.flight;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check Customer objects against arbitrary requirements.</p>
 */
public class FlightValidator{
	@Inject //validator dependency
	private Validator validator;
	
    @Inject //automatic logging dependency
    private @Named("logger") Logger log;

    @Inject //calls crud dependency
    private FlightRepository crud;
    
    void validate(Flight flight) throws ConstraintViolationException, ValidationException {
    	//creates a bean to validate flight data.
        Set<ConstraintViolation<Flight>> violations = validator.validate(flight);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        if (flightAlreadyExists(flight)) { //check for duplicate flight numbers
            throw new FlightExistsException("Flight number already exists");
        }
    }
    
    boolean flightAlreadyExists(Flight flight) { //checks if the flight exists in the database
    	if(flight == null) {
    		return false;
    	}
    	//checks if flight exists in the database based on flight number
    	Flight fromDb = null;
    	String number = flight.getNumber();

    	try{
    		fromDb = crud.findByNumber(number);
    		//if a flight matches the flight number
    		return fromDb != null && fromDb.equals(flight);
    	} catch (NonUniqueResultException e) {
    		log.warning("The flight number : " + flight + " already exists in the Database");
    		return true;
    	}
    }
}