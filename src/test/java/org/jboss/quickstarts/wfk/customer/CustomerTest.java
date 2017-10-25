package org.jboss.quickstarts.wfk.customer;

import static org.junit.Assert.*;

import java.io.File;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerRestService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CustomerTest {
	
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
                .addClasses(Customer.class, CustomerRestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
    
    
    @Inject //calls customer rest service
    CustomerRestService customerRestService;
    
    @Test
    @InSequence(1) //test 1 - creates a valid customer and checks its persistence
    public void createValidCustomer() {
    	Customer customer = new Customer(null, "Georgios Rovolis", "rovolisgiorgos@gmail.com", "07871545186");
    	
    	Response response = customerRestService.createCustomer(customer);
    	assertEquals("Unexpected response status", 201, response.getStatus());
    }
    
    @Test
    @InSequence(2) //test 2 creates invalid customers and throw exception
    public void createInvalidCustomers() {
    	Customer[] customers = new Customer[]{
    		//tests name length
    		new Customer(null, "Geoooooooooooooooooooooooooooooooooooooorgiosssssssssssssssssss Rovolisssssssssssssss", "rovolisgiorgos@gmail.com", "07871545186"),
    		
    		new Customer(null, "Georgios Rovolis", "rovolisgiorgos!gmail.com", "07871545186"), //test email format
    		new Customer(null, "Georgios Rovolis", "rovolisgiorgos@gmail.com", "+447871545186") //uk phone number format
    	};
    	
    	for(Customer customer : customers) {
    		try {
    			customerRestService.createCustomer(customer);
    			fail("Expected a RestServiceException to be thrown");
    		} catch (RestServiceException e) {
    			assertFalse("Internal server error shouldn't really happen", e.getStatus() == Response.Status.INTERNAL_SERVER_ERROR);
    		}
    	}
    }
    
    @Test
    @InSequence(3) //test 3 - create duplicate customers and throw exception
    public void createDuplicateCustomers() {
    	Customer customerCopy = new Customer(null, "Georgios Rovolis", "rovolisgiorgos@gmail.com", "07871545186");
    	
    	try {
    		customerRestService.createCustomer(customerCopy);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
    	}
    }
        
    @Test
    @InSequence(4) // test 4 - invalid customer id and throw exception
    public void getCustomerByInvalidId() {
    	try {
    		customerRestService.getCustomer(null);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    }
    
    @Test
    @InSequence(5) //test 5 - customer information update
    public void updateValidCustomer() {
    	Customer customer = new Customer(null, "Georgios Rovolis", "rovolisgiorgos3@gmail.com", "07871545186");
    	Response response;
    	
    	response = customerRestService.createCustomer(customer);
    	customer = (Customer) response.getEntity();
    	
    	assertFalse("Customer ID not received", customer.getId() == null);
    	
    	customer.setName("Georgios Test");
    	response = customerRestService.updateCustomer(customer.getId(), customer);
    	
    	customer = (Customer) response.getEntity();
    	assertTrue("Customer information update failed", customer.getName().equals("Georgios Test"));
    }
    
    @Test
    @InSequence(6) //test 6 - create customer and then delete 
    public void deleteValidCustomer() {
    	Customer customer = new Customer(null, "Georgios Rovolis", "rovolisgiorgos5@gmail.com", "07871545186");
    	Response response;
    	
    	response = customerRestService.createCustomer(customer);
    	customer = (Customer) response.getEntity();
    	
    	response = customerRestService.deleteCustomer(customer.getId());
    	assertEquals("Unexpected response status", 204, response.getStatus());
    }
    
}