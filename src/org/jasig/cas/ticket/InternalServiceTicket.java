package org.jasig.cas.ticket;


/**
 * Interface for ServiceTickets within the CentralAuthenticationService domain.
 * 
 * This interface exposes methods for internal use that the CentralAuthenticationService
 * may use to update the state of the ticket.
 * 
 * @author Scott battaglia
 * @version $Id$
 *
 */
public interface InternalServiceTicket extends ServiceTicket {
	
    TicketGrantingTicket grantTicketGrantingTicket();
}
