package org.jboss.quickstarts.wfk.customer;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import org.jboss.quickstarts.wfk.booking.Booking;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Customer.class)
public abstract class Customer_ {

	public static volatile SingularAttribute<Customer, String> phoneNumber;
	public static volatile SingularAttribute<Customer, String> name;
	public static volatile SingularAttribute<Customer, Long> id;
	public static volatile SetAttribute<Customer, Booking> bookings;
	public static volatile SingularAttribute<Customer, String> email;

}

