/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Interface for Tickets within the CentralAuthenticationService domain.
 * 
 * This interface exposes methods for internal use that the CentralAuthenticationService
 * may use to update the state of the ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface InternalTicket extends Ticket {
	
	int getCountOfUses();
	
	long getLastTimeUsed();
}
