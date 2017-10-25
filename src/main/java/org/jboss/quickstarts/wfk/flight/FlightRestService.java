package org.jboss.quickstarts.wfk.flight;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.quickstarts.wfk.util.RestServiceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Path("/flights") //REST Web Service using JAX-RS
@Consumes(MediaType.APPLICATION_JSON) //returns json
@Produces(MediaType.APPLICATION_JSON) //returns json
@Api(value = "/flights") //points to the resource flights
@Stateless //It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow transaction
//demarcation when accessing the database." - Antonio Goncalves
public class FlightRestService {
	@Inject //calls the flight service dependency
	FlightService service;
	
    @Inject //automatic logging depedency
    private @Named("logger") Logger log;
    
	
    @GET //returns all stored flights in a array
    @ApiOperation(value = "Fetch all flights", notes = "Returns a JSON array of all stored Flights")
	public Response getAllFlights() {
		List<Flight> flights = service.findAll();
		return Response.ok(flights).build();
	}
    
    @POST
    @ApiOperation(value = "Adds a Flight to the Database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Flight has been created successfully."),
            @ApiResponse(code = 400, message = "Invalid flight details inserted"),
            @ApiResponse(code = 409, message = "Flight details inserted come in conflict with an existing flight"),
            @ApiResponse(code = 500, message = "Unexpected error occured")
    })
	public Response createFlight(
		@ApiParam(value = "JSON output of the flights", required = true) 
		Flight flight) {
    	
        if (flight == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder;

        try {
            //adds a flight
            service.create(flight);

            //The request has been fulfilled and resulted in a new resource being created
            builder = Response.status(Response.Status.CREATED).entity(flight);

        } catch (ConstraintViolationException ce) {
        	//bean validation - throws rest service exception
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
        } catch (FlightExistsException e) {
        	// deals with the unique flight number violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("number", "Flight number inserted already exists, use a different flight number");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
        	// throws rest of the exceptions that do not apply to the above
        	log.warning("Caught internal exception when adding a flight: " + e.getMessage());
            throw new RestServiceException(e);
        }
    	
        return builder.build();
	}
    
    
    /**
     * <p>Deletes a flight using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Flight to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE //deletes flight by a specified flight number
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Deletes a Flight from the Database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Flight has been deleted successfuly"),
            @ApiResponse(code = 400, message = "Flight ID inserted is not valid"),
            @ApiResponse(code = 404, message = "Flight ID does not exist"),
            @ApiResponse(code = 500, message = "Unexpected error occured")
    })
    public Response deleteFlight(
            @ApiParam(value = "Flight ID specified to deletetion", allowableValues = "range[0, infinity]", required = true)
            @PathParam("ID")
            Long id) {

        Response.ResponseBuilder builder;

        Flight flight;
        
        if (id == null || (flight = service.findById(id)) == null) { //verifies that the flight number exists
            throw new RestServiceException("No Flight with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(flight); //deletes flight
            builder = Response.noContent();
        } catch (Exception e) {
        	//throws generic exception
            throw new RestServiceException(e);
        }
        
        return builder.build();
    }

}
