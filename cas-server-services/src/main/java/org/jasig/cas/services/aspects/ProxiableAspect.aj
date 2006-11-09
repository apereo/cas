/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.aspects;

import org.jasig.cas.services.domain.AccessDeniedException;
import org.jasig.cas.services.domain.RegisteredService;
import org.jasig.cas.services.service.ServiceManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.CentralAuthenticationService;

/**
 * Aspect to determine whether a service is allowed to proxy or not.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public aspect ProxiableAspect {

    /** Instance of Service Manager. */
    private ServiceManager serviceManager;

    /** Instance of TicketRegistry. */
    private TicketRegistry ticketRegistry;

    public void setServiceManager(final ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    before(final String ticketId) : execution(* CentralAuthenticationService+.delegateTicketGrantingTicket(String, Credentials)) && args(ticketId, Credentials) {
        final ServiceTicket ticket = (ServiceTicket) this.ticketRegistry
            .getTicket(ticketId);
        final RegisteredService registeredService = this.serviceManager
            .getServiceByUrl(ticket.getService().getId());

        if (registeredService == null || !registeredService.isEnabled()) {
            throw new AccessDeniedException("service: "
                + ticket.getService().getId() + " is not allowed to Proxy.");
        }
    }
}
