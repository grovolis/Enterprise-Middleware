package org.jboss.quickstarts.wfk.booking;

import java.io.Serializable;


public class GuestBooking implements Serializable { //implements deserialisation for the Jackson JSON library that used both booking and customers
	private static final long serialVersionUID = 1L;

	Booking booking;
	
	public Booking getBooking() { //returns booking object
		return booking;
	}
	
	public void setBooking(Booking booking) { //sets booking object
		this.booking = booking;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuestBooking)) return false;
        GuestBooking guestBooking = (GuestBooking) o;

        return booking.equals(guestBooking.getBooking()) && booking.getCustomer().equals(guestBooking.getBooking().getCustomer());
    }

    @Override
    public int hashCode() {
    	int hash = 17;
        hash = hash * 31 + booking.hashCode();
        hash = hash * 31 + booking.getCustomer().hashCode();
    	return hash;
    }
}
