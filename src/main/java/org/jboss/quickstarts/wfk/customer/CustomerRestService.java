package org.jboss.quickstarts.wfk.customer;

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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.quickstarts.wfk.util.RestServiceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Path("/customers") //REST Web Service using JAX-RS
@Consumes(MediaType.APPLICATION_JSON) //returns json
@Produces(MediaType.APPLICATION_JSON) //returns json
@Api(value = "/customers") //points to the resource customers
@Stateless //It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow transaction
//demarcation when accessing the database." - Antonio Goncalves
public class CustomerRestService {
	@Inject //assigns dependency
	CustomerService service;
	
    @Inject //assigns dependency
    private @Named("logger") Logger log;
    
	
    @GET //returns all stored customers in a array
    @ApiOperation(value = "Fetch all Customers", notes = "Returns a JSON array of all stored Customers")
	public Response getAllCustomers() {
		List<Customer> customers = service.findAllOrderedByName();
		return Response.ok(customers).build();
	}
    
    @GET
    @Path("/{id:[0-9]+}") //returns customer by a specific ID
    @ApiOperation(value = "Fetch Customer's information by ID", notes = "Returns a Customer")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Customer's ID is found"),
            @ApiResponse(code = 400, message = "ID Format is invalid"),
            @ApiResponse(code = 404, message = "No Customer found with the specified ID")
    })
	public Response getCustomer(@PathParam("ID") Long id) {
    	
    	Customer customer = null;
    	
    	if(id == null || (customer = service.findById(id)) == null) { //check id is not null and return customer with the specific ID
    		throw new RestServiceException("ID: " + id + " does not belong to any customers", Response.Status.NOT_FOUND);
    	}
    	
		return Response.ok(customer).build();
	}
    
    
    @POST
    @ApiOperation(value = "Adds Customer to the Database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Customer has been created successfully."),
            @ApiResponse(code = 400, message = "Invalid Customer details provided"),
            @ApiResponse(code = 409, message = "Customer details inserted are not unique"),
            @ApiResponse(code = 500, message = "Unexpected error occured")
    })
	public Response createCustomer(
		@ApiParam(value = "JSON output of the customer", required = true) 
		Customer customer) {
    	
        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder;

        try {
            //adds a new customer
            service.create(customer);

            //The request has been fulfilled and resulted in a new resource being created
            builder = Response.status(Response.Status.CREATED).entity(customer);

        } catch (ConstraintViolationException ce) {
            //bean validation - throws rest service exception
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (CustomerExistsException e) {
            // deals with the unique mail violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "Email already in use");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // throws rest of the exceptions that do not apply to the above
        	log.warning("Internal exception upon adding a customer: " + e.getMessage());
        	e.printStackTrace();
            throw new RestServiceException(e);
        }
    	
        return builder.build();
	}
    
    
    @PUT
    @Path("/{id:[0-9]+}")
    @ApiOperation(value= "Updates Customer's details in the database")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "Customer has been updated successfuly"),
    		@ApiResponse(code = 400, message = "Invalid Customer ID inserted"),
    		@ApiResponse(code = 404, message = "ID inserted does not match any customers"),
    		@ApiResponse(code = 409, message = "Customer's email already in use")
    })
    public Response updateCustomer(
    	@ApiParam(value = "Customer ID has been updated", allowableValues = "range[0, infinity]", required = true)
    	@PathParam("id") Long id,
    	@ApiParam(value = "JSON output of the customer to be updated", required = true)
    	Customer updatedCustomer) {
    	
    	if(updatedCustomer == null) {
    		throw new RestServiceException("Invalid Customer inserted", Response.Status.BAD_REQUEST);
    	}
    	
    	if (updatedCustomer.getId() != null && !updatedCustomer.getId().equals(id)) {
            // ID of the customer cannot be updated
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The Customer ID must be the same with the one of the updated Customer");
            throw new RestServiceException("Customer details inserted creates a conflict with another Customer",
                    responseObj, Response.Status.NOT_FOUND);
    	}
    	
    	if(service.findById(id) == null) { //verifies that the customer ID exists in the DB
            throw new RestServiceException("Customer ID: " + id + " not found in the Database", Response.Status.NOT_FOUND);
    	}
        
        Response.ResponseBuilder builder;

        try {
            //updates the customer
            service.update(updatedCustomer);
            builder = Response.ok(updatedCustomer);

        } catch (ConstraintViolationException ce) {
            //bean validation - throws rest service exception
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (CustomerExistsException e) {
            // deals with the unique mail violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // throws rest of the exceptions that do not apply to the above
        	log.warning("Internal exception upon updating a customer: " + e.getMessage());
            throw new RestServiceException(e);
        }
        
        return builder.build();
    }
    
    
    /**
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Customer to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}") //deletes a customer using an ID (Long id)
    @ApiOperation(value = "Deletes a Customer from the Database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Customer has been deleted successfuly"),
            @ApiResponse(code = 404, message = "Customer's ID does not exist"),
            @ApiResponse(code = 500, message = "Unexpected error occured")
    })
    public Response deleteCustomer(
            @ApiParam(value = "Customer's ID to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            Long id) {

        Response.ResponseBuilder builder;

    	Customer customer = null;
    	
    	if(id == null || (customer = service.findById(id)) == null) {
    		throw new RestServiceException("Customer's ID: " + id + " does not exist in the Database", Response.Status.NOT_FOUND);
    	}

        try {
            service.delete(customer); //deletes customer
            builder = Response.noContent();
        } catch (Exception e) {
            throw new RestServiceException(e); //throws generic exception
        }
        
        return builder.build();
    }

}
