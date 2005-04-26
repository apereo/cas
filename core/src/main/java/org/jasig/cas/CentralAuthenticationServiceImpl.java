/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.TicketNotFoundException;
import org.jasig.cas.ticket.TicketValidationException;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;

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
public final class CentralAuthenticationServiceImpl implements
    CentralAuthenticationService {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final Log log = LogFactory.getLog(this.getClass());

    /** TicketRegistry for storing and retrieving tickets as needed. */
    private TicketRegistry ticketRegistry;

    /**
     * AuthenticationManager for authenticating credentials for purposes of
     * obtaining tickets.
     */
    private AuthenticationManager authenticationManager;

    /** UniqueTicketIdGenerator to generate ids for any tickets created. */
    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets. */
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets. */
    private ExpirationPolicy serviceTicketExpirationPolicy;

    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        if (ticketGrantingTicketId == null) {
            throw new IllegalArgumentException(
                "ticketGrantingTicketId cannot be null");
        }

        synchronized (this.ticketRegistry) {
            log.debug("Removing ticket [" + ticketGrantingTicketId
                + "] from registry.");
            final TicketGrantingTicket ticket = (TicketGrantingTicket) this.ticketRegistry
                .getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

            if (ticket != null) {
                log.debug("Ticket found.  Expiring and then deleting.");
                ticket.expire();
                this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
            }
        }
    }

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service, final Credentials credentials)
        throws TicketException {

        if (ticketGrantingTicketId == null || service == null) {
            throw new IllegalArgumentException(
                "ticketGrantingTicketId, credentials and service are required fields.");
        }

        final TicketGrantingTicket ticketGrantingTicket;
        synchronized (this.ticketRegistry) {
            ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry
                .getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

            if (ticketGrantingTicket == null
                || ticketGrantingTicket.isExpired()) {
                throw new TicketNotFoundException();
            }

            if (credentials != null) {
                try {
                    Authentication authentication = this.authenticationManager
                        .authenticate(credentials);

                    Principal originalPrincipal = ticketGrantingTicket
                        .getAuthentication().getPrincipal();
                    Principal newPrincipal = authentication.getPrincipal();

                    if (!newPrincipal.equals(originalPrincipal)) {
                        throw new TicketCreationException();
                    }
                } catch (AuthenticationException e) {
                    throw new TicketCreationException(e);
                }
            }

            final ServiceTicket serviceTicket = ticketGrantingTicket
                .grantServiceTicket(this.uniqueTicketIdGenerator
                    .getNewTicketId(ServiceTicket.PREFIX), service,
                    this.serviceTicketExpirationPolicy);

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

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service) throws TicketException {
        return this.grantServiceTicket(ticketGrantingTicketId, service, null);
    }

    public String delegateTicketGrantingTicket(final String serviceTicketId,
        final Credentials credentials) throws TicketException {

        if (serviceTicketId == null || credentials == null) {
            throw new IllegalArgumentException(
                "serviceTicketId and credentials are required fields.");
        }

        try {
            final Authentication authentication = this.authenticationManager
                .authenticate(credentials);

            final ServiceTicket serviceTicket;
            synchronized (this.ticketRegistry) {
                serviceTicket = (ServiceTicket) this.ticketRegistry.getTicket(
                    serviceTicketId, ServiceTicket.class);

                if (serviceTicket == null || serviceTicket.isExpired()) {
                    throw new TicketNotFoundException();
                }

                TicketGrantingTicket ticketGrantingTicket = serviceTicket
                    .grantTicketGrantingTicket(this.uniqueTicketIdGenerator
                        .getNewTicketId(TicketGrantingTicket.PREFIX),
                        authentication,
                        this.ticketGrantingTicketExpirationPolicy);

                this.ticketRegistry.addTicket(ticketGrantingTicket);

                return ticketGrantingTicket.getId();
            }
        } catch (AuthenticationException e) {
            throw new TicketCreationException(e);
        }
    }

    public Assertion validateServiceTicket(final String serviceTicketId,
        final Service service) throws TicketException {
        if (serviceTicketId == null || service == null) {
            throw new IllegalArgumentException(
                "serviceTicketId, and service cannot be null.");
        }

        synchronized (this.ticketRegistry) {
            final ServiceTicket serviceTicket = (ServiceTicket) this.ticketRegistry
                .getTicket(serviceTicketId, ServiceTicket.class);

            if (serviceTicket == null) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] does not exist.");
                throw new TicketNotFoundException();
            }

            if (serviceTicket.isExpired()) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] has expired.");
                throw new TicketValidationException();
            }

            serviceTicket.incrementCountOfUses();
            serviceTicket.updateLastTimeUsed();

            if (!service.equals(serviceTicket.getService())) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] does not match supplied service.");
                throw new TicketValidationException();
            }

            return new ImmutableAssertionImpl(serviceTicket.getGrantingTicket()
                .getChainedPrincipals(), serviceTicket.isFromNewLogin());
        }
    }

    public String createTicketGrantingTicket(final Credentials credentials)
        throws TicketCreationException {
        log.debug("Attempting to create TicketGrantingTicket for "
            + credentials);
        if (credentials == null) {
            throw new IllegalArgumentException(
                "credentials is a required field.");
        }

        try {
            final Authentication authentication = this.authenticationManager
                .authenticate(credentials);

            synchronized (this.ticketRegistry) {
                final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
                    this.uniqueTicketIdGenerator
                        .getNewTicketId(TicketGrantingTicket.PREFIX),
                    authentication, this.ticketGrantingTicketExpirationPolicy);

                this.ticketRegistry.addTicket(ticketGrantingTicket);

                return ticketGrantingTicket.getId();
            }
        } catch (AuthenticationException e) {
            throw new TicketCreationException(e);
        }
    }

    /**
     * Method to set the TicketRegistry.
     * 
     * @param ticketRegistry the TicketRegistry to set.
     */
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
