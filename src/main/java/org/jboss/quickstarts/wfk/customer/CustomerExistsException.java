package org.jboss.quickstarts.wfk.customer;
import javax.validation.ValidationException;

//checks if customer's email is used for another customer
public class CustomerExistsException extends ValidationException {
	private static final long serialVersionUID = 1L;
	
	public CustomerExistsException(String message) {
		super(message);
	}
	
	public CustomerExistsException(String message, Throwable e) {
		super(message, e);
	}
	
	public CustomerExistsException(Throwable e) {
		super(e);
	}
}