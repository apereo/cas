/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jasig.cas.services.AuthenticatedService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.SingleSignoutCallback;
import org.jasig.cas.ticket.ServiceTicket;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;

/**
 * Class to advise the removal of Tickets from the Ticket Registry.  Watches the tickets being
 * requested to be deleted and checks if its in its list to apply single-sign out to.  If it is,
 * it sends single sign out requests to any service associated with that ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * 
 * @see org.jasig.cas.services.SingleSignoutCallback
 * @see org.jasig.cas.services.ServiceRegistry
 * @see org.jasig.cas.services.AuthenticatedService
 * @see org.jasig.cas.ticket.registry.TicketRegistry
 */
public class CallbackServicesOnTicketGrantingTicketDestructionAfterReturningAdvice
		implements AfterReturningAdvice, InitializingBean {

	private ServiceRegistry serviceRegistry;
	
	private Map singleSignoutMapping;
	
	
	public void afterReturning(final Object returnValue, final Method method, final Object[] args,
			final Object target) throws Throwable {
		final String ticketGrantingTicketId = (String) args[0];
		final Set servicesToCallback;

		synchronized (this.singleSignoutMapping) {
			if (!this.singleSignoutMapping.containsKey(ticketGrantingTicketId)) {
				return;
			}
			
			servicesToCallback = (Set) this.singleSignoutMapping.get(ticketGrantingTicketId);
			this.singleSignoutMapping.remove(ticketGrantingTicketId);
		}
		
		for (final Iterator iter = servicesToCallback.iterator(); iter.hasNext();) {
			final ServiceTicket serviceTicket = (ServiceTicket) iter.next();
			final AuthenticatedService service = this.serviceRegistry.getService(serviceTicket.getService().getId());
			final SingleSignoutCallback callback = service.getSingleSignoutCallback();
			
			if (callback != null ){
				callback.sendSingleSignoutRequest(service, serviceTicket.getId());
			}
		}
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}


	public void setSingleSignoutMapping(Map singleSignoutMapping) {
		this.singleSignoutMapping = singleSignoutMapping;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.serviceRegistry == null) {
			throw new IllegalStateException("serviceRegistry cannot be null on " + this.getClass().getName());
		}
		
		if (this.singleSignoutMapping == null) {
			throw new IllegalStateException("singleSignoutMapping cannot be null on " + this.getClass().getName());
		}
	}
	

}
