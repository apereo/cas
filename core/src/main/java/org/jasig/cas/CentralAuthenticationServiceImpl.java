/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.Authentication;
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
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.AssertionImpl;

/**
 * Concrete implementation of a CentralAuthenticationService, and also the
 * central, organizing component of CAS's internal implementation.
 * <p>
 * This class is threadsafe.
 * 
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @author Dmitry Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class CentralAuthenticationServiceImpl implements CentralAuthenticationService {

    private final Log log = LogFactory.getLog(this.getClass());

    /** TicketRegistry for storing and retrieving tickets as needed. */
    private TicketRegistry ticketRegistry;

    /**
     * AuthenticationManager for authenticating credentials for purposes of
     * obtaining tickets.
     */
    private AuthenticationManager authenticationManager;

    /** UniqueTicketIdGenerator to generate ids for any tickets created */
    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets */
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets */
    private ExpirationPolicy serviceTicketExpirationPolicy;

    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        synchronized (this.ticketRegistry) {
            try {
                log.debug("Removing ticket [" + ticketGrantingTicketId
                    + "] from registry.");
                final TicketGrantingTicket ticket = (TicketGrantingTicket)this.ticketRegistry
                    .getTicket(ticketGrantingTicketId,
                        TicketGrantingTicket.class);

                if (ticket != null) {
                    log.debug("Ticket found.  Expiring and then deleting.");
                    ticket.expire();
                    this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
                }
            }
            catch (InvalidTicketClassException ite) {
                log.debug("Invalid request to remove ticket ["
                    + ticketGrantingTicketId
                    + "].  Ticket not a valid TicketGrantingTicket.");
                throw new IllegalArgumentException(
                    "ticketGrantingTicketId must be the ID of a TicketGrantingTicket");
            }
        }
    }

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service, Credentials credentials)
        throws AuthenticationException, TicketCreationException {

        try {
            final TicketGrantingTicket ticketGrantingTicket;
            synchronized (this.ticketRegistry) {
                ticketGrantingTicket = (TicketGrantingTicket)this.ticketRegistry
                    .getTicket(ticketGrantingTicketId,
                        TicketGrantingTicket.class);

                if (ticketGrantingTicket == null
                    || ticketGrantingTicket.isExpired())
                    return null;

                if (credentials != null) {
                    Authentication authentication = this.authenticationManager
                        .authenticateAndResolveCredentials(credentials);

                    Principal originalPrincipal = ticketGrantingTicket
                        .getAuthentication().getPrincipal();
                    Principal newPrincipal = authentication.getPrincipal();

                    if (!newPrincipal.equals(originalPrincipal))
                        return null;
                }

                final ServiceTicket serviceTicket = ticketGrantingTicket
                    .grantServiceTicket(service);

                // TODO we need a better way of handling this
                if (credentials != null) {
                    serviceTicket.setFromNewLogin(true);
                }

                this.ticketRegistry.addTicket(serviceTicket);

                log.info("Granted service ticket ["
                    + serviceTicket.getId()
                    + "] for service ["
                    + service.getId()
                    + "] for user ["
                    + serviceTicket.getGrantingTicket().getAuthentication()
                        .getPrincipal().getId() + "]");

                return serviceTicket.getId();
            }
        }
        catch (InvalidTicketClassException ite) {
            throw new TicketCreationException(
                "Unable to retrieve TicketGrantingTicket to grant service ticket.");
        }
    }

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service) throws TicketCreationException {
        try {
            return this.grantServiceTicket(ticketGrantingTicketId, service,
                null);
        }
        catch (AuthenticationException e) {
            // this should not happen as authentication is never done from here.
            log.error(e);
            return null;
        }
    }

    public String delegateTicketGrantingTicket(final String serviceTicketId,
        final Credentials credentials) throws AuthenticationException,
        TicketException {
        final Authentication authentication = this.authenticationManager
            .authenticateAndResolveCredentials(credentials);

        final ServiceTicket serviceTicket;
        synchronized (this.ticketRegistry) {
            serviceTicket = (ServiceTicket)this.ticketRegistry.getTicket(
                serviceTicketId, ServiceTicket.class);

            if (serviceTicket == null || serviceTicket.isExpired())
                return null;

            TicketGrantingTicket ticketGrantingTicket = serviceTicket
                .grantTicketGrantingTicket(authentication);

            this.ticketRegistry.addTicket(ticketGrantingTicket);

            return ticketGrantingTicket.getId();
        }
    }

    public Assertion validateServiceTicket(final String serviceTicketId,
        final Service service) throws TicketException {
        if (serviceTicketId == null || service == null) {
            throw new IllegalArgumentException(
                "serviceTicketId, service and authenticationSpecification cannot be null.");
        }

        synchronized (this.ticketRegistry) {
            final ServiceTicket serviceTicket = (ServiceTicket)this.ticketRegistry
                .getTicket(serviceTicketId, ServiceTicket.class);

            if (serviceTicket == null) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] does not exist.");
                throw new TicketException(TicketException.INVALID_TICKET,
                    "ticket '" + serviceTicketId + "' not recognized");
            }

            if (serviceTicket.isExpired()) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] has expired.");
                throw new TicketException(TicketException.INVALID_TICKET,
                    "ticket '" + serviceTicketId + "' not recognized");
            }
			
            serviceTicket.incrementCountOfUses();
            serviceTicket.updateLastTimeUsed();

            if (!service.equals(serviceTicket.getService())) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] does not match supplied service.");
                throw new TicketException(TicketException.INVALID_SERVICE,
                    "ticket '" + serviceTicketId
                        + "' does not match supplied service");
            }

            return new AssertionImpl(serviceTicket.getGrantingTicket()
                .getChainedPrincipals(), serviceTicket.isFromNewLogin());
        }
    }

    public String createTicketGrantingTicket(final Credentials credentials)
        throws AuthenticationException {
        final Authentication authentication = this.authenticationManager
            .authenticateAndResolveCredentials(credentials);
        final TicketGrantingTicket ticketGrantingTicket;

        synchronized (this.ticketRegistry) {
            ticketGrantingTicket = new TicketGrantingTicketImpl(
                this.uniqueTicketIdGenerator
                    .getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication, this.ticketGrantingTicketExpirationPolicy,
                this.uniqueTicketIdGenerator,
                this.serviceTicketExpirationPolicy);

            this.ticketRegistry.addTicket(ticketGrantingTicket);

            return ticketGrantingTicket.getId();
        }
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    /**
     * @param authenticationManager The authenticationManager to set.
     */
    public void setAuthenticationManager(
        final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @param ticketGrantingTicketExpirationPolicy The
     * ticketGrantingTicketExpirationPolicy to set.
     */
    public void setTicketGrantingTicketExpirationPolicy(
        final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
    }

    /**
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to use
     */
    public void setUniqueTicketIdGenerator(
        final UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }

    /**
     * @param serviceTicketExpirationPolicy The serviceTicketExpirationPolicy to
     * set.
     */
    public void setServiceTicketExpirationPolicy(
        final ExpirationPolicy serviceTicketExpirationPolicy) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
    }
}