/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import java.util.Date;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.RegistryCleaner;



/**
 * Class to look for expired tickets and remove them from the registry.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class DefaultTicketRegistryCleaner implements RegistryCleaner {
	protected final Log log = LogFactory.getLog(getClass());
	private TicketRegistry ticketRegistry;

	/**
	 * @see org.jasig.cas.util.RegistryCleaner#clean()
	 */
	public void clean() {
		this.log.info("Starting cleaning of expired tickets from ticket registry at [" + new Date() + "]");
		synchronized (this.ticketRegistry)
		{
			for (Iterator iter = this.ticketRegistry.getTickets().iterator(); iter.hasNext();)
			{
				final Ticket ticket = (Ticket) iter.next();
				
				if (ticket.isExpired())
					iter.remove();
			}
		}
		this.log.info("Finished cleaning of expired tickets from ticket registry at [" + new Date() + "]");
	}
	/**
	 * @param ticketRegistry The ticketRegistry to set.
	 */
	public void setTicketRegistry(final TicketRegistry ticketRegistry) {
		this.ticketRegistry = ticketRegistry;
	}
}
