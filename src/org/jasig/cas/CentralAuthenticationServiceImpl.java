/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.Assertion;
import org.jasig.cas.authentication.AssertionImpl;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.Service;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.InvalidTicketClassException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

/**
 * Implementation of a CentralAuthenticationService.
 * 
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @author Dmitry Kopylenko
 */
public final class CentralAuthenticationServiceImpl extends ServletEndpointSupport implements CentralAuthenticationService {

    private final Log log = LogFactory.getLog(this.getClass());

    /** TicketRegistry for storing and retrieving tickets as needed. **/
    private TicketRegistry ticketRegistry;

    /** AuthenticationManager for authenticating credentials for purposes of obtaining tickets. **/
    private AuthenticationManager authenticationManager;

    /** UniqueTicketIdGenerator to generate ids for any tickets created **/
    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets **/
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets */
    private ExpirationPolicy serviceTicketExpirationPolicy;

    /**
     * @see org.jasig.cas.CentralAuthenticationService#destroyTicketGrantingTicket(java.lang.String)
     */
    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        try {
            final TicketGrantingTicket ticket = (TicketGrantingTicket)this.ticketRegistry.getTicket(ticketGrantingTicketId,
                TicketGrantingTicket.class);

            log.debug("Removing ticket [" + ticketGrantingTicketId + "] from registry.");
            if (ticket != null) {
                ticket.expire();
                this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
            }
        } catch (InvalidTicketClassException ite) {
            log.debug("Invalid request to remove ticket [" + ticketGrantingTicketId + "].  Ticket not a valid TicketGrantingTicket.");
            throw new IllegalArgumentException("ticketGrantingTicketId must be the ID of a TicketGrantingTicket");
        }
    }

    /**
     * @see org.jasig.cas.CentralAuthenticationService#grantServiceTicket(java.lang.String, org.jasig.cas.Service)
     */
    public String grantServiceTicket(final String ticketGrantingTicketId, final Service service) throws TicketCreationException {
        try {
            final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

            if (ticketGrantingTicket == null)
                return null;

            final ServiceTicket serviceTicket = ticketGrantingTicket.grantServiceTicket(service);

            this.ticketRegistry.addTicket(serviceTicket);

            log.info("Granted service ticket [" + serviceTicket.getId() + "] for service [" + service.getId() + "] for user ["
                + serviceTicket.getGrantingTicket().getPrincipal().getId() + "]");

            return serviceTicket.getId();
        } catch (InvalidTicketClassException ite) {
            throw new TicketCreationException("Unable to retrieve TicketGrantingTicket to grant service ticket.");
        }
    }

    /**
     * @see org.jasig.cas.CentralAuthenticationService#grantTicketGrantingTicket(java.lang.String, org.jasig.cas.authentication.principal.Credentials)
     */
    public String delegateTicketGrantingTicket(final String serviceTicketId, final Credentials credentials) throws AuthenticationException, TicketException {
        final Principal principal = this.authenticationManager.authenticateAndResolveCredentials(credentials);
        
        if (principal == null) {
            return null;
        }
        
        final ServiceTicket serviceTicket = (ServiceTicket) this.ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class);
        
        TicketGrantingTicket ticketGrantingTicket = serviceTicket.grantTicketGrantingTicket(principal);
        
        this.ticketRegistry.addTicket(ticketGrantingTicket);
        
        return ticketGrantingTicket.getId();
    }

    /**
     * @see org.jasig.cas.CentralAuthenticationService#validateServiceTicket(java.lang.String, org.jasig.cas.Service,
     * org.jasig.cas.authentication.AuthenticationSpecification)
     */
    public Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws TicketException {
        if (serviceTicketId == null || service == null) {
            throw new IllegalArgumentException("serviceTicketId, service and authenticationSpecification cannot be null.");
        }

        final ServiceTicket serviceTicket = (ServiceTicket) this.ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

        if (serviceTicket == null) {
            log.debug("ServiceTicket [" + serviceTicketId + "] does not exist.");
            throw new TicketException(TicketException.INVALID_TICKET, "ticket '" + serviceTicketId + "' not recognized");
        }

        synchronized (serviceTicket) {
            if (serviceTicket.isExpired()) {
                log.debug("ServiceTicket [" + serviceTicketId + "] has expired.");
                throw new TicketException(TicketException.INVALID_TICKET, "ticket '" + serviceTicketId + "' not recognized");
            }
    
            if (!service.equals(serviceTicket.getService())) {
                log.debug("ServiceTicket [" + serviceTicketId + "] does not match supplied service.");
                throw new TicketException(TicketException.INVALID_SERVICE, "ticket '" + serviceTicketId + "' does not match supplied service");
            }
    
            serviceTicket.incrementCountOfUses();
        }

        return new AssertionImpl(serviceTicket.getGrantingTicket().getChainedPrincipals(), serviceTicket.isFromNewLogin()); // TODO handle proxy case
    }

    /**
     * @see org.jasig.cas.CentralAuthenticationService#grantTicketGrantingTicket(org.jasig.cas.authentication.principal.Principal)
     */
    public String createTicketGrantingTicket(final Credentials credentials) throws AuthenticationException {
        final Principal principal = this.authenticationManager.authenticateAndResolveCredentials(credentials);
        final TicketGrantingTicket ticketGrantingTicket;

        if (principal == null)
            return null;

        ticketGrantingTicket = new TicketGrantingTicketImpl(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), principal,
            this.ticketGrantingTicketExpirationPolicy, this.uniqueTicketIdGenerator, this.serviceTicketExpirationPolicy);

        this.ticketRegistry.addTicket(ticketGrantingTicket);

        return ticketGrantingTicket.getId();
    }

    /**
     * @see org.jasig.cas.CentralAuthenticationService#getVersion()
     */
    public String getVersion() {
        // Fetches the "Implementation-Version" manifest attribute from the jar file.
        return CasVersion.class.getPackage().getImplementationVersion();
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    /**
     * @param authenticationManager The authenticationManager to set.
     */
    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @param ticketGrantingTicketExpirationPolicy The ticketGrantingTicketExpirationPolicy to set.
     */
    public void setTicketGrantingTicketExpirationPolicy(final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
    }

    /**
     * @param ticketGrantingTicketUniqueTicketIdGenerator The ticketGrantingTicketUniqueTicketIdGenerator to set.
     */
    public void setUniqueTicketIdGenerator(final UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }

    /**
     * @param serviceTicketExpirationPolicy The serviceTicketExpirationPolicy to set.
     */
    public void setServiceTicketExpirationPolicy(final ExpirationPolicy serviceTicketExpirationPolicy) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
    }
}
