/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;

/**
 * Class to watch for ServiceTickets being entered into the registry and adding them to the list of ticktets
 * to watch for Single Signout capabilities.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class MonitorServiceTicketsAfterReturningAdvice implements
		AfterReturningAdvice, InitializingBean {
	
	private TicketRegistry ticketRegistry;
	
	private Map singleSignoutMapping;

	public void afterReturning(final Object returnObject, final Method method, final Object[] args,
			final Object target) throws Throwable {
		
		final Ticket ticket = (Ticket) args[0];
		
		if (!(ticket instanceof ServiceTicket)) {
			return;
		}
		
		final ServiceTicket serviceTicket = (ServiceTicket) ticket;
		final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
		
		synchronized (this.singleSignoutMapping) {
		
			if (this.singleSignoutMapping.containsKey(ticketGrantingTicket.getId())) {
				Set serviceTickets = (Set) this.singleSignoutMapping.get(ticketGrantingTicket);
				serviceTickets.add(serviceTicket);
			} else {
				Set serviceTickets = new HashSet();
				serviceTickets.add(serviceTicket);
				this.singleSignoutMapping.put(ticketGrantingTicket.getId(), serviceTickets);
			}
		}
	}

	public void setSingleSignoutMapping(Map singleSignoutMapping) {
		this.singleSignoutMapping = singleSignoutMapping;
	}

	public void setTicketRegistry(TicketRegistry ticketRegistry) {
		this.ticketRegistry = ticketRegistry;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.ticketRegistry == null) {
			throw new IllegalStateException("ticketRegistry cannot be null on " + this.getClass().getName());
		}
		
		if (this.singleSignoutMapping == null) {
			throw new IllegalStateException("singleSignoutMapping cannot be null on " + this.getClass().getName());
		}
	}
}
