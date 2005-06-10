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
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketValidationException;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Concrete implementation of a CentralAuthenticationService, and also the
 * central, organizing component of CAS's internal implementation.
 * <p>
 * This class is threadsafe.
 * <p>
 * This class has the following properties that must be set:
 * <ul>
 * <li> <code>ticketRegistry</code> - The Ticket Registry to maintain the list
 * of available tickets.</li>
 * <li> <code>authenticationManager</code> - The service that will handle
 * authentication.</li>
 * <li> <code>ticketGrantingTicketUniqueTicketIdGenerator</code> - Plug in to
 * generate unique secure ids for TicketGrantingTickets.</li>
 * <li> <code>serviceTicketUniqueTicketIdGenerator</code> - Plug in to
 * generate unique secure ids for ServiceTickets.</li>
 * <li> <code>ticketGrantingTicketExpirationPolicy</code> - The expiration
 * policy for TicketGrantingTickets.</li>
 * <li> <code>serviceTicketExpirationPolicy</code> - The expiration policy for
 * ServiceTickets.</li>
 * </ul>
 * 
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @author Dmitry Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class CentralAuthenticationServiceImpl implements
    CentralAuthenticationService, InitializingBean {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final Log log = LogFactory.getLog(this.getClass());

    /** TicketRegistry for storing and retrieving tickets as needed. */
    private TicketRegistry ticketRegistry;

    /**
     * AuthenticationManager for authenticating credentials for purposes of
     * obtaining tickets.
     */
    private AuthenticationManager authenticationManager;

    /**
     * UniqueTicketIdGenerator to generate ids for TicketGrantingTickets
     * created.
     */
    private UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** UniqueTicketIdGenerator to generate ids for ServiceTickets created. */
    private UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets. */
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets. */
    private ExpirationPolicy serviceTicketExpirationPolicy;

    /**
     * Implementation of destoryTicketGrantingTicket expires the ticket provided
     * and removes it from the TicketRegistry.
     * 
     * @throws IllegalArgumentException if the TicketGrantingTicket ID is null.
     */
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

    /**
     * @throws IllegalArgumentException if TicketGrantingTicket ID, Credentials
     * or Service are null.
     */
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
                throw new InvalidTicketException();
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
                .grantServiceTicket(this.serviceTicketUniqueTicketIdGenerator
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

    /**
     * @throws IllegalArgumentException if the ServiceTicketId or the
     * Credentials are null.
     */
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
                    throw new InvalidTicketException();
                }

                TicketGrantingTicket ticketGrantingTicket = serviceTicket
                    .grantTicketGrantingTicket(
                        this.ticketGrantingTicketUniqueTicketIdGenerator
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

    /**
     * @throws IllegalArgumentException if the ServiceTicketId or the Service
     * are null.
     */
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
                throw new InvalidTicketException();
            }

            if (serviceTicket.isExpired()) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] has expired.");
				this.ticketRegistry.deleteTicket(serviceTicketId);
                throw new InvalidTicketException();
            }

            serviceTicket.incrementCountOfUses();
            serviceTicket.updateLastTimeUsed();
			
			/* implemented this manual removal if expired so that registry 
			 * does not grow too large.
			 */
			if (serviceTicket.isExpired()) {
				this.ticketRegistry.deleteTicket(serviceTicketId);
			}

            if (!service.equals(serviceTicket.getService())) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] does not match supplied service.");
                throw new TicketValidationException();
            }

            return new ImmutableAssertionImpl(serviceTicket.getGrantingTicket()
                .getChainedAuthentications(), serviceTicket.getService(),
                serviceTicket.isFromNewLogin());
        }
    }

    /**
     * @throws IllegalArgumentException if the credentials are null.
     */
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
                    this.ticketGrantingTicketUniqueTicketIdGenerator
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
     * Method to inject the AuthenticationManager into the class.
     * 
     * @param authenticationManager The authenticationManager to set.
     */
    public void setAuthenticationManager(
        final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Method to inject the TicketGrantingTicket Expiration Policy.
     * 
     * @param ticketGrantingTicketExpirationPolicy The
     * ticketGrantingTicketExpirationPolicy to set.
     */
    public void setTicketGrantingTicketExpirationPolicy(
        final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
    }

    /**
     * Method to inject the Unique Ticket Id Generator into the class.
     * 
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to use
     */
    public void setTicketGrantingTicketUniqueTicketIdGenerator(
        final UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.ticketGrantingTicketUniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }

    /**
     * Method to inject the TicketGrantingTicket Expiration Policy.
     * 
     * @param serviceTicketExpirationPolicy The serviceTicketExpirationPolicy to
     * set.
     */
    public void setServiceTicketExpirationPolicy(
        final ExpirationPolicy serviceTicketExpirationPolicy) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
    }

    public void afterPropertiesSet() throws Exception {
        final String name = this.getClass().getName();
        Assert.notNull(this.ticketRegistry, "ticketRegistry cannot be null on "
            + name);
        Assert.notNull(this.authenticationManager,
            "authenticationManager cannot be null on " + name);
        Assert.notNull(this.ticketGrantingTicketUniqueTicketIdGenerator,
            "ticketGrantingTicketUniqueTicketIdGenerator cannot be null on " + name);
        Assert.notNull(this.serviceTicketUniqueTicketIdGenerator,
            "serviceTicketUniqueTicketIdGenerator cannot be null on " + name);
        Assert.notNull(this.ticketGrantingTicketExpirationPolicy,
            "ticketGrantingTicketExpirationPolicy cannot be null on " + name);
        Assert.notNull(this.serviceTicketExpirationPolicy,
            "serviceTicketExpirationPolicy cannot be null on " + name);
    }

    public void setServiceTicketUniqueTicketIdGenerator(
        UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator) {
        this.serviceTicketUniqueTicketIdGenerator = serviceTicketUniqueTicketIdGenerator;
    }

}
