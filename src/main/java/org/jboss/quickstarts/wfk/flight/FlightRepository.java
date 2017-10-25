package org.jboss.quickstarts.wfk.flight;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

public class FlightRepository {
    @Inject
    private EntityManager em;
    
    List<Flight> findAll() {
        TypedQuery<Flight> query = em.createNamedQuery(Flight.FIND_ALL, Flight.class); //returns all flight sorted by flight number
        return query.getResultList();
    }

    Flight findByNumber(String number) throws NonUniqueResultException { //returns flight with the requested flight number
        TypedQuery<Flight> query = em.createNamedQuery(Flight.FIND_BY_NUMBER, Flight.class).setParameter("number", number);
        Flight result = null;
        
        try{
        	result = query.getSingleResult();
        } catch (NoResultException e) {} //if there is not flight with that flight number or more than one throws exception
        
        return result;
    }
    
    Flight findById(Long id) { //returns flight by its number
    	return em.find(Flight.class, id);
    }
    
    
    /**
     * <p>Writes the provided Flight object to the application database.<p/>
     *
     * <p>Validates the data in the provided Flight object using a {@link FlightValidator} object.<p/>
     *
     * @param flight The Flight object to be written to the database using a {@link FlightRepository} object
     * @return The Flight object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Flight create(Flight flight) throws ConstraintViolationException, ValidationException, Exception {
        //stores the flight to the database
        em.persist(flight);

        return flight;
    }
    
    Flight delete(Flight flight) throws Exception {
    	
        if (flight.getId() != null) {
            em.remove(em.merge(flight)); //deletes the flight from the database
        }

        return flight;
    }
}
