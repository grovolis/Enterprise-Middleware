package org.jboss.quickstarts.wfk.booking;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;

public class BookingService {
	@Inject //call retrieve update delete dependency
	BookingRepository crud;
	
	@Inject //validator dependency
	BookingValidator validator;
	
    public List<Booking> findAll() { //returns all booking objects sorted by booking number
        return crud.findAll();
    }
    
    public Booking findById(Long id) { //returns the booking that matches the ID
    	return crud.findById(id);
    }
    
    public List<Booking> findByCustomer(Customer customer) {
        return crud.findByCustomer(customer); //returns all bookings made by a specific customer
    }
    
    public Booking findByFlight(Flight flight, Date flightDate) throws NonUniqueResultException {
        return crud.findByFlight(flight, flightDate); //returns booking made on a specific date for a specific flight
    } //if no bookings are found throws exception
    
    public Booking create(Booking booking) throws ConstraintViolationException, ValidationException, Exception {
    	//Validates the the data to be passed to the booking entity match the parameters needed
    	validator.validate(booking);
    	
        //stores the booking into the database
        return crud.create(booking);
    }
    
    public Booking delete(Booking booking) throws Exception {
        return crud.delete(booking); //deletes the booking for the database
    }
}
