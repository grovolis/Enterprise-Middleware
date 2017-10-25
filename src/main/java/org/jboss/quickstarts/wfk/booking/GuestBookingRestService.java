package org.jboss.quickstarts.wfk.booking;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.quickstarts.wfk.customer.CustomerExistsException;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.util.RestServiceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/guestbookings") //REST Web Service using JAX-RS
@Consumes(MediaType.APPLICATION_JSON) //returns json
@Produces(MediaType.APPLICATION_JSON) //returns json
@Api(value = "/guestbookings") //points to the resource guestbookings
@Stateless //It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow transaction
//demarcation when accessing the database." - Antonio Goncalves
@TransactionManagement(TransactionManagementType.BEAN) //manually manage transaction boundaries inside the class
public class GuestBookingRestService {

	@Resource //calls resource userTransaction
	UserTransaction userTransaction;
	
	@Inject //calls booking service dependency
	BookingService bookingService;
	
	@Inject //calls customer service dependency
	CustomerService customerService;
	
	
    @POST
    @ApiOperation(value = "Adds the booking and its customer to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Guest Booking has been created successfully."),
            @ApiResponse(code = 400, message = "Guest Booking details inserted is invalid"),
            @ApiResponse(code = 409, message = "Guest Booking details inserted comes in conflict with an existing Guest Booking"),
            @ApiResponse(code = 500, message = "Unexpected error occured")
    })
	public Response createGuestBooking(
			@ApiParam(value = "JSON output of the customer and the booking to be added to the Database", required = true) 
			GuestBooking guestBooking) {

    	//check booking input for errors
    	if(guestBooking == null || guestBooking.getBooking() == null || guestBooking.getBooking().getCustomer() == null) {
    		throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
    	}
    	
    	try {
        	try {
    			userTransaction.begin(); //begins the transaction
    			
    			customerService.create(guestBooking.getBooking().getCustomer());
    			bookingService.create(guestBooking.getBooking());
    			
    			userTransaction.commit(); 
        	} catch (CustomerExistsException | BookingExistsException e) { //checks if booking or customers already exists
        		userTransaction.rollback();
        		throw new RestServiceException(e.getMessage(), Response.Status.CONFLICT, e); //throws exception
        	} catch (FlightInvalidException e) { //check for invalid flight information
        		userTransaction.rollback();
        		throw new RestServiceException(e.getMessage(), Response.Status.NOT_FOUND, e);
        	} catch (ConstraintViolationException e) {
        		//bean validation - throws rest service exception
                Map<String, String> responseObj = new HashMap<>();

                for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                    responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
                }
                
                //stops transaction and throws exception
                userTransaction.rollback();
                throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);	
        	} catch (Exception e) {
        		 //stops transaction and throws exception
                userTransaction.rollback();
        		throw new RestServiceException(e);
			}
    	} catch (SystemException e) {
    		throw new RestServiceException(e);
    	}
    	// The request has been fulfilled and resulted in a new resource being created
    	Response.ResponseBuilder builder = Response.status(Response.Status.CREATED).entity(guestBooking);
    	return builder.build();
	}
}
