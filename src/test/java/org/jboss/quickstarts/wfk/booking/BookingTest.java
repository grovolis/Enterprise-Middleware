package org.jboss.quickstarts.wfk.booking;

import static org.junit.Assert.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerRestService;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.flight.FlightRestService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BookingTest {
	
    @Deployment
    public static Archive<?> createTestArchive() { //contains all necessary external dependencies to run the tests
        //HttpComponents and org.JSON are required by ContactService
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.codehaus.jackson:jackson-core-asl:1.9.9",
                "org.codehaus.jackson:jackson-mapper-asl:1.9.9",
                "org.codehaus.jackson:jackson-jaxrs:1.9.9"
        ).withTransitivity().asFile();

        Archive<?> archive = ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(true, "org.jboss.quickstarts.wfk")
                .addAsLibraries(libs)
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("arquillian-ds.xml")
                .addClasses(Booking.class, Flight.class, Customer.class, BookingRestService.class, FlightRestService.class, CustomerRestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
    
    @Inject //calls booking rest service
    BookingRestService bookingRestService;
    
    @Inject //calls flight rest service
    FlightRestService flightRestService;
    
    @Inject //calls customer rest service
    CustomerRestService customerRestService;
    
    @Inject //calls guest booking rest service
    GuestBookingRestService guestBookingRestService;
    
    
    @Test
    @InSequence(1) //test 1 - valid booking and check of persistence
    public void createValidBooking() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Booking booking = new Booking(
    			addCustomer(new Customer(null, "Georgios Papadopoulos", "geopap@gmail.com", "07881545185")),
    			addFlight(new Flight(null, "GR502", "NCL", "GRE")),
    			sdf.parse("2016-12-31")
    	);
    	
		Response response = bookingRestService.createBooking(booking);
		assertEquals("Unexpected response status", 201, response.getStatus());
    }
    
    @Test
    @InSequence(2) //test 2 - creates a duplicate booking and throws exception
    public void createDuplicateBookings() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Booking booking = new Booking(
    			addCustomer(new Customer(null, "Giannis Papagiannis", "giapapa@gmail.com", "07871545181")),
    			addFlight(new Flight(null, "GR556", "NCL", "DNK")),
    			sdf.parse("2016-12-31")
    	);
    	
		booking = (Booking) bookingRestService.createBooking(booking).getEntity();
		
		booking.setId(null);
    	try {
    		bookingRestService.createBooking(booking);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
    	}
    }
    
    @Test
    @InSequence(3) // test 3 - creates a booking and then deletes it
    public void deleteValidBooking() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Booking booking = new Booking(
    			addCustomer(new Customer(null, "Georgios Rovolis", "rovolisgiorgos15@gmail.com", "07871545186")),
    			addFlight(new Flight(null, "BK040", "NRL", "DNE")),
    			sdf.parse("2016-12-31")
    	);
    	
		booking = (Booking) bookingRestService.createBooking(booking).getEntity();
		
		Response response = bookingRestService.deleteBooking(booking.getId());
		assertEquals("Unexpected response status", 204, response.getStatus());
    }
    
    /**
     * Delete booking that doesn't exist and check if exception is thrown
     */
    @Test
    @InSequence(4) // test 4 - try to delete a non existing booking and throw exception 
    public void deleteInvalidBooking() {
    	try {
    		bookingRestService.deleteBooking(null);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    	
    	try {
    		bookingRestService.deleteBooking(new Long(0));
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    }
    
    @Test
    @InSequence(5) //test 5 - upon deleting a commodity all assosiated booking are also deleted
    @SuppressWarnings("unchecked")
    public void deleteCommodityCascade() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Customer customer1 = addCustomer(new Customer(null, "Georgios Rovolis", "rovolisgiorgos16@gmail.com", "07871545186"));
    	Customer customer2 = addCustomer(new Customer(null, "Georgios Rovolis", "rovolisgiorgos17@gmail.com", "07871545186"));
    	Flight flight = addFlight(new Flight(null, "GR888", "NCL", "ATH"));
    	
    	Booking[] bookings = new Booking[] {
			new Booking(
	    			customer1,
	    			flight,
	    			sdf.parse("2016-12-31")
	    	),
			new Booking(
	    			customer1,
	    			flight,
	    			sdf.parse("2016-12-30")
	    	),
			new Booking(
	    			customer2,
	    			flight,
	    			sdf.parse("2016-12-29")
	    	)
    	};
    	
    	//creates different customers for a certain booking
    	for(Booking booking : bookings) {
    		Booking result = (Booking) bookingRestService.createBooking(booking).getEntity();
    		assertFalse("Booking ID not received", result.getId() == null);
    	}
    	
    	//validate that all bookings for a specific customer were stored in the database
		List<Booking> customer1Bookings;
		customer1Bookings = (List<Booking>) bookingRestService.getAllBookings(customer1.getId()).getEntity();
    	assertEquals("1st Customer is not assigned to all the bookings", customer1Bookings.size(), 2);
    	
    	
		List<Booking> customer2Bookings;
		customer2Bookings = (List<Booking>) bookingRestService.getAllBookings(customer2.getId()).getEntity();
    	assertEquals("2nd Customer is not assigned to all the bookings", customer2Bookings.size(), 1);
    	
    }
        
    private Customer addCustomer(Customer c) {
    	Response response = customerRestService.createCustomer(c);
    	return (Customer) response.getEntity();
    }
    
    private Flight addFlight(Flight f) {
    	Response response = flightRestService.createFlight(f);
    	return (Flight) response.getEntity();
    }
}
