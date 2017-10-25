package org.jboss.quickstarts.wfk.flight;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import org.jboss.quickstarts.wfk.booking.Booking;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Flight.class)
public abstract class Flight_ {

	public static volatile SingularAttribute<Flight, String> number;
	public static volatile SingularAttribute<Flight, String> destination;
	public static volatile SingularAttribute<Flight, Long> id;
	public static volatile SingularAttribute<Flight, String> departure;
	public static volatile SetAttribute<Flight, Booking> bookings;

}

