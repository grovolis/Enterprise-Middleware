package org.jboss.quickstarts.wfk.booking;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
//import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;
//import org.jboss.quickstarts.wfk.travelagent.TravelAgentBooking;

import io.swagger.annotations.ApiModelProperty;

//This class illustrates the booking information stored in the database
//it also retrieves customer info from the database (@NamedQueries) and sets constraints for the table fields
/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({ //finds all bookings
        @NamedQuery(name = Booking.FIND_ALL, query = "SELECT b FROM Booking b"),
        @NamedQuery(name = Booking.FIND_BY_FLIGHT, query = "SELECT b FROM Booking b where b.flight.id = :flight_id AND b.bookingDate = :bookingDate"),
        @NamedQuery(name = Booking.FIND_BY_CUSTOMER, query= "SELECT b FROM Booking b where b.customer.id = :customer_id")
})
@XmlRootElement
@Table(name = "booking", uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "booking_date"})) //assigns flight id and booking date as a key to the table
public class Booking implements Serializable {
	private static final long serialVersionUID = 1L; //this is a default value to remove warnings
	
	public static final String FIND_ALL = "Booking.findAll"; //finds all bookings 
	public static final String FIND_BY_FLIGHT = "Booking.findByFlight"; //finds bookings by flight 
	public static final String FIND_BY_CUSTOMER = "Booking.findByCustomer"; //finds bookings by customer

    @ApiModelProperty(hidden=true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //sets unique ID and increment to the number
	private Long id;
    
    @ManyToOne //database relation to customer id
    @JoinColumn(name="customer_id")
	private Customer customer;
	
	@ManyToOne //database relation to flight id
	@JoinColumn(name="flight_id")
	private Flight flight;
	
    @NotNull //date of booking cannot be null
    @Temporal(TemporalType.DATE)
    @Future
    @Column(name="booking_date", nullable=false)
	private Date bookingDate;
    
    public Booking() {}
    
    public Booking(Customer customer, Flight flight, Date date) { //booking constructor
    	this.customer = customer;
    	this.flight = flight;
    	this.bookingDate = date;
    }
    //assign values
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Flight getFlight() {
		return flight;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        
        return (
        		bookingDate.equals(booking.getBookingDate()) && 
        		flight.equals(booking.getFlight())
        );
    }

    @Override
    public int hashCode() {
    	int hash = 17;
        hash = hash * 31 + bookingDate.hashCode();
        hash = hash * 31 + flight.hashCode();
    	return hash;
    }
}
