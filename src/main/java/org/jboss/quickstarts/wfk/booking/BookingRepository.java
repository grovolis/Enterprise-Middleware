package org.jboss.quickstarts.wfk.booking;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;

public class BookingRepository {
    @Inject
    private EntityManager em;
    
    List<Booking> findAll() { //returns all booking objects sorted by booking id
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_ALL, Booking.class);
        return query.getResultList();
    }
    
    Booking findById(Long id) { //returns the booking according to the id requested
    	return em.find(Booking.class, id);
    }
    
    List<Booking> findByCustomer(Customer customer) { //returns all booking made by a specific customer
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_CUSTOMER, Booking.class).setParameter("customer_id", customer.getId());
        return query.getResultList();
    }
    
    Booking findByFlight(Flight flight, Date bookingDate) throws NonUniqueResultException { //finds booking specified by flight and date
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_FLIGHT, Booking.class).setParameter("flight_id", flight.getId()).setParameter("bookingDate", bookingDate);
        Booking result = null;
        
        try{
        	result = query.getSingleResult();
        } catch (NoResultException e) {} //if more than one booking are found (or none) throws exception
        
        return result;
    }
    
    Booking create(Booking booking) throws ConstraintViolationException, ValidationException, Exception { //validates data used in booking
        //adds booking to the database
        em.persist(booking);

        return booking;
    }
    
    Booking delete(Booking booking) throws Exception {
    	
        if (booking.getId() != null) { //deletes booking from the database
            em.remove(em.merge(booking));
        }

        return booking;
    }
}
