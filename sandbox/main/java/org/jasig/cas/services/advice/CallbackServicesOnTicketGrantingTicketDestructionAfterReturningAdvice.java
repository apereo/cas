/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jasig.cas.services.ServiceRegistry;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;

/**
 * Class to advise the removal of Tickets from the Ticket Registry. Watches the
 * tickets being requested to be deleted and checks if its in its list to apply
 * single-sign out to. If it is, it sends single sign out requests to any
 * service associated with that ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * @see org.jasig.cas.services.SingleSignoutCallback
 * @see org.jasig.cas.services.ServiceRegistry
 * @see org.jasig.cas.services.CallbackRegisteredService
 * @see org.jasig.cas.ticket.registry.TicketRegistry
 */
public final class CallbackServicesOnTicketGrantingTicketDestructionAfterReturningAdvice
    implements AfterReturningAdvice, InitializingBean {

    /** The registry containing the list of services. */
    private ServiceRegistry serviceRegistry;

    /** The mapping of TicketGrantingTickets to service tickets. */
    private Map singleSignoutMapping;

    public void afterReturning(final Object returnValue, final Method method,
        final Object[] args, final Object target) throws Throwable {
        final String ticketGrantingTicketId = (String) args[0];
        final Set servicesToCallback;

        synchronized (this.singleSignoutMapping) {
            if (!this.singleSignoutMapping.containsKey(ticketGrantingTicketId)) {
                return;
            }

            servicesToCallback = (Set) this.singleSignoutMapping
                .get(ticketGrantingTicketId);
            this.singleSignoutMapping.remove(ticketGrantingTicketId);
        }

        for (final Iterator iter = servicesToCallback.iterator(); iter
            .hasNext();) {
//            final ServiceTicket serviceTicket = (ServiceTicket) iter.next();
//            final RegisteredService service = this.serviceRegistry
//                .getService(serviceTicket.getService().getId());
            // TODO: renable later
       /*     final SingleSignoutCallback callback = service != null ? service
                .getSingleSignoutCallback() : null;

            if (callback != null) {
                callback.signOut(service, serviceTicket.getId());
            }*/
        }
    }

    /**
     *  Set the service registry.
     * @param serviceRegistry the service registry to use.
     */
    public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Set the single signout map.
     * @param singleSignoutMapping the map of single sign outs to use.
     */
    public void setSingleSignoutMapping(final Map singleSignoutMapping) {
        this.singleSignoutMapping = singleSignoutMapping;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.serviceRegistry == null) {
            throw new IllegalStateException(
                "serviceRegistry cannot be null on "
                    + this.getClass().getName());
        }

        if (this.singleSignoutMapping == null) {
            throw new IllegalStateException(
                "singleSignoutMapping cannot be null on "
                    + this.getClass().getName());
        }
    }
}
