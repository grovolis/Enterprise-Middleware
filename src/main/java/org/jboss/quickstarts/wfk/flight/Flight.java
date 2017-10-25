package org.jboss.quickstarts.wfk.flight;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.util.CompareStrings;
import org.jboss.quickstarts.wfk.util.StringComparisonMode;

//This class illustrates the flight information is stored in the database
//it also retrieves flight info from the database (@NamedQueries) and sets constraints for the table fields

/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({ //finds all fights or by number
        @NamedQuery(name = Flight.FIND_ALL, query = "SELECT f FROM Flight f ORDER BY f.number ASC"),
        @NamedQuery(name = Flight.FIND_BY_NUMBER, query = "SELECT f FROM Flight f WHERE f.number = :number")
})
@XmlRootElement
@Table(name = "flight", uniqueConstraints = @UniqueConstraint(columnNames = "number")) //assigns flight number as unique key to the table
@CompareStrings(propertyNames={"departure", "destination"}, 
				matchMode=StringComparisonMode.NOT_EQUAL_IGNORE_CASE, 
				message="The departure and destination must be different")
public class Flight implements Serializable {
	private static final long serialVersionUID = 1L; //this is a default value to remove warnings
	
    public static final String FIND_ALL = "Flight.findAll";
    public static final String FIND_BY_NUMBER = "Flight.findByNumber";
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //sets unique ID and increment to the number
	private Long id; 
	
    @NotEmpty //sets flight number constraints 
    @NotNull
    @Size(min = 5, max = 5)
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Flight number should be an alpha-numerical string of 5 characters")
	private String number; //primary key
	
    @NotEmpty //departure airport constraints 
    @NotNull
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]+$", message = "Flight departure airport code should be 3 upper letter characters")
    @Column(name = "departure")
	private String departure;

    @NotNull //destination airport constraints
    @NotEmpty
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]+$", message = "Flight arrival airport code should be 3 upper letter characters")
    @Column(name = "destination")
	private String destination;
    
    @JsonIgnore
    @OneToMany(mappedBy="flight", orphanRemoval=true) //when object is removed applies cascade removal to the flight
    private Set<Booking> bookings = new HashSet<Booking>();
    
    public Flight() { }
    
    public Flight(Long id, String number, String departure, String destination) { //flight constructor
    	this.id = id;
    	this.number = number;
    	this.departure = departure;
    	this.destination = destination;
    }
    //assign values
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNumber() {
    	return number;
    }
    
    public void setNumber(String number) {
    	this.number = number;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public Set<Booking> getBookings() {
    	return bookings;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight)) return false;
        Flight flight = (Flight) o;
        if (!number.equalsIgnoreCase(flight.getNumber())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }
    
    @Override
    public String toString() {
    	return String.format("Flight[%s]: %s from %s to %s", id, number, departure, destination);
    }
}
