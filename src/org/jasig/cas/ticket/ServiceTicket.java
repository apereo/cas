/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;


/**
 * Interface for a Service Ticket.  A service ticket is used to grant access
 * to a specific service.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface ServiceTicket extends Ticket
{
	String getService();

	boolean isFromNewLogin();
	
	TicketGrantingTicket getGrantor();
}
