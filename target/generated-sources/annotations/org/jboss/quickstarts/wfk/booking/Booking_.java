package org.jboss.quickstarts.wfk.booking;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Booking.class)
public abstract class Booking_ {

	public static volatile SingularAttribute<Booking, Flight> flight;
	public static volatile SingularAttribute<Booking, Date> bookingDate;
	public static volatile SingularAttribute<Booking, Long> id;
	public static volatile SingularAttribute<Booking, Customer> customer;

}

