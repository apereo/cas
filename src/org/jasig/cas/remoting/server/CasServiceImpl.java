/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.server;

import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationRequest;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.remoting.CasService;
import org.jasig.cas.ticket.CasAttributes;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketManager;

/**
 * Default implementation of the CasService
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class CasServiceImpl implements CasService {

    private TicketManager ticketManager;

    private AuthenticationManager authenticationManager;

    /**
     * @see org.jasig.cas.remoting.CasService#getServiceTicket(java.lang.String, java.lang.String)
     */
    public String getServiceTicket(String ticketGrantingTicketId, String service) {
        final TicketGrantingTicket ticket;
        final CasAttributes casAttributes = new CasAttributes();
        final ServiceTicket serviceTicket;
        
        ticket = ticketManager.validateTicketGrantingTicket(null); // TODO currently we don't support renew!
        
        if (ticket == null)
            return null;
        
        casAttributes.setService(service);
        serviceTicket = ticketManager.createServiceTicket(casAttributes, ticket);
        
        if (serviceTicket == null)
            return null;
        
        return serviceTicket.getId();
    }
    /**
     * @see org.jasig.cas.remoting.CasService#getTicketGrantingTicket(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public String getTicketGrantingTicket(AuthenticationRequest request) {
        // TODO validation
        final Principal principal;
        final TicketGrantingTicket ticket;
        
        principal = authenticationManager.authenticateUser(request);
        
        if (principal == null)
            return null;
        
        ticket = ticketManager.createTicketGrantingTicket(principal, null);
        
        return ticket.getId();
    }

    /**
     * @param authenticationManager The authenticationManager to set.
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @param ticketManager The ticketManager to set.
     */
    public void setTicketManager(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }
}
