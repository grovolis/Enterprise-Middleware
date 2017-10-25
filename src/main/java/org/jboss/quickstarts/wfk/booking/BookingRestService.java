package org.jboss.quickstarts.wfk.booking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.util.RestServiceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Path("/bookings") //REST Web Service using JAX-RS
@Consumes(MediaType.APPLICATION_JSON) //returns json
@Produces(MediaType.APPLICATION_JSON) //returns json
@Api(value = "/bookings") //points to the resource bookings
@Stateless//It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow transaction
//demarcation when accessing the database." - Antonio Goncalves
public class BookingRestService {
	@Inject //assigns dependency
	BookingService service;
	
	@Inject //assigns dependency
	CustomerService customerService;
	
    @Inject //assigns dependency
    private @Named("logger") Logger log;
    
    
    @GET
    @ApiOperation(value = "Fetch all Bookings", notes = "Returns a JSON array of all stored Bookings")
	public Response getAllBookings(
			@ApiParam(value = "Customer's ID", allowableValues = "range[0, infinity]", required = false)
			@QueryParam("customerId") Long customerId) {
    	List<Booking> bookings; //lists bookings
    	
    	if(customerId == null) {
    		bookings = service.findAll();
    	} else {
        	Customer customer = customerService.findById(customerId); //finds bookings by customer id
        	
        	if(customer != null) {
        		bookings = service.findByCustomer(customer);
        	} else {
        		bookings = new ArrayList<Booking>(0);
        	}
    	}
		
		return Response.ok(bookings).build();
	}
    
    @POST
    @ApiOperation(value = "Adds a Booking to the Database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Booking has been added successfully"),
            @ApiResponse(code = 400, message = "Invalid Booking details inserted"),
            @ApiResponse(code = 409, message = "Booking details inserted come in conflict with an existing Booking"),
            @ApiResponse(code = 500, message = "Unexpected error occured")
    })
	public Response createBooking(
		@ApiParam(value = "JSON output of the Bookings", required = true) 
		Booking booking) {
    	
        if (booking == null || booking.getCustomer() == null || booking.getFlight() == null || booking.getBookingDate() == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder;

        try {
            //adds a new booking
            service.create(booking);
            

            // The request has been fulfilled and resulted in a new resource being created
            builder = Response.status(Response.Status.CREATED).entity(booking);

        } catch (ConstraintViolationException ce) {
            //bean validation - throws rest service exception
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
        } catch (CustomerInvalidException e) {
            //checks if customer id exists
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("customer", "Customer's ID inserted does not exist, use a valid Customer ID");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.NOT_FOUND, e);
        } catch (FlightInvalidException e) {
            //check for flight id
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("flight", "Flight ID inserted does not exist, use a valid Flight ID");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.NOT_FOUND, e);
        } catch (BookingExistsException e) {
            //check if flight id is in use
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("number", "Flight ID inserted is already in use, use a different combination of flight ID and date");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
        	log.warning("Internal exception upon adding a booking: " + e.getMessage());
            throw new RestServiceException(e);
		}
    	
        return builder.build();
	}
    
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Deletes a Booking from the Database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Booking has been deleted successfuly"),
            @ApiResponse(code = 404, message = "Booking ID does not exist"),
            @ApiResponse(code = 500, message = "Unexpeceted error occured")
    })
    public Response deleteBooking( //deletes a specified booking
            @ApiParam(value = "Booking ID to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("ID")
            Long id) {

        Response.ResponseBuilder builder;

        Booking booking;
        
        if (id == null || (booking = service.findById(id)) == null) { //verifies that the bookings exists in the DB
            throw new RestServiceException("Booking ID: " + id + " does not exist", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(booking);
            builder = Response.noContent();
        } catch (Exception e) {
        	//throws generic exception
            throw new RestServiceException(e);
        }
        
        return builder.build();
    }

}
