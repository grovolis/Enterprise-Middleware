package org.jboss.quickstarts.wfk.flight;

import static org.junit.Assert.*;

import java.io.File;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class FlightTest {

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
                .addClasses(Flight.class, FlightRestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
    
    
    @Inject //calls the fight rest service
    FlightRestService flightRestService;
    
    @Test
    @InSequence(1) //test 1 - creates and valid flight and check persistence
    public void createValidFlight() {
    	Flight flight = new Flight(null, "GR999", "NCL", "ATH");
    	Response response = flightRestService.createFlight(flight);
    	assertEquals("Unexpected response status", 201, response.getStatus());
    }
    
    /**
     * Create invalid flights and check if exception is thrown
     */
    @Test
    @InSequence(2) //test 2 - creates invalid flight formats and throws exception
    public void createInvalidFlights() {
    	Flight[] flights = new Flight[] {
    		new Flight(null, "", "NCL", "ATH"), //flight number test
    		new Flight(null, "GR999", "NCLL", "ATH"), //departure airport code test
    		new Flight(null, "GR999", "NCL", "ATHH"), //arrival airport code test
    		new Flight(null, "GR999", "NCL", "NCL") //departure and arrival cannot be the same
    	};
    	
    	for(Flight flight : flights) {
    		try {
    			flightRestService.createFlight(flight);
    			fail("Expected a RestServiceException to be thrown");
    		} catch (RestServiceException e) {
    			assertFalse("Internal error should not be thrown", e.getStatus() == Response.Status.INTERNAL_SERVER_ERROR);
    		}
    	}
    }
    
    @Test
    @InSequence(3) //test 3 - duplicate flights throw exception
    public void createDuplicateFlights() {
    	Flight flightCopy = new Flight(null, "GR999", "NCL", "ATH");
    	
    	try {
    		flightRestService.createFlight(flightCopy);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected error occured", Response.Status.CONFLICT, e.getStatus());
    	}
    }
    
    @Test
    @InSequence(4) //Test 4 - creates a flight and then deletes it
    public void deleteValidFlight() {
    	Flight flight = new Flight(null, "GR104", "NCL", "ATH");
    	Response response;
    	
    	response = flightRestService.createFlight(flight);
    	flight = (Flight) response.getEntity();
    	
    	response = flightRestService.deleteFlight(flight.getId());
    	assertEquals("Unexpected error occured", 204, response.getStatus());
    }
}
