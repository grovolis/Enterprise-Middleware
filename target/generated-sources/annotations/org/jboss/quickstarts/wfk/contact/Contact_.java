package org.jboss.quickstarts.wfk.contact;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Contact.class)
public abstract class Contact_ {

	public static volatile SingularAttribute<Contact, String> firstName;
	public static volatile SingularAttribute<Contact, String> lastName;
	public static volatile SingularAttribute<Contact, String> phoneNumber;
	public static volatile SingularAttribute<Contact, Long> id;
	public static volatile SingularAttribute<Contact, String> state;
	public static volatile SingularAttribute<Contact, Date> birthDate;
	public static volatile SingularAttribute<Contact, String> email;

}

