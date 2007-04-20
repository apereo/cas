/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.services.Attribute;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.services.UnauthorizedSsoServiceException;
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
import org.jasig.cas.util.annotation.NotNull;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;
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
    CentralAuthenticationService {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final Log log = LogFactory.getLog(this.getClass());

    /** TicketRegistry for storing and retrieving tickets as needed. */
    @NotNull
    private TicketRegistry ticketRegistry;

    /**
     * AuthenticationManager for authenticating credentials for purposes of
     * obtaining tickets.
     */
    @NotNull
    private AuthenticationManager authenticationManager;

    /**
     * UniqueTicketIdGenerator to generate ids for TicketGrantingTickets
     * created.
     */

    @NotNull
    private UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Map to contain the mappings of service->UniqueTicketIdGenerators */
    @NotNull
    private Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    /** Expiration policy for ticket granting tickets. */
    @NotNull
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets. */
    @NotNull
    private ExpirationPolicy serviceTicketExpirationPolicy;

    /** Implementation of Service Registry */
    @NotNull
    private ServicesManager servicesManager;

    /**
     * Implementation of destoryTicketGrantingTicket expires the ticket provided
     * and removes it from the TicketRegistry.
     * 
     * @throws IllegalArgumentException if the TicketGrantingTicket ID is null.
     */
    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        Assert.notNull(ticketGrantingTicketId);

        if (log.isDebugEnabled()) {
            log.debug("Removing ticket [" + ticketGrantingTicketId
                + "] from registry.");
        }
        final TicketGrantingTicket ticket = (TicketGrantingTicket) this.ticketRegistry
            .getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        if (ticket != null) {
            if (log.isDebugEnabled()) {
                log.debug("Ticket found.  Expiring and then deleting.");
            }
            ticket.expire();
            this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
        }
    }

    /**
     * @throws IllegalArgumentException if TicketGrantingTicket ID, Credentials
     * or Service are null.
     */
    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service, final Credentials credentials)
        throws TicketException {

        Assert.notNull(ticketGrantingTicketId,
            "ticketGrantingticketId cannot be null");
        Assert.notNull(service, "service cannot be null");

        final TicketGrantingTicket ticketGrantingTicket;
        ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry
            .getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        if (ticketGrantingTicket == null) {
            throw new InvalidTicketException();
        }

        synchronized (ticketGrantingTicket) {
            if (ticketGrantingTicket.isExpired()) {
                this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
                throw new InvalidTicketException();
            }
        }

        final RegisteredService registeredService = this.servicesManager
            .findServiceBy(service);

        if (registeredService == null) {
            if (log.isDebugEnabled()) {
                log.debug("Service [" + service.getId()
                    + "] not found in ServiceRegistry.");
            }
            throw new UnauthorizedServiceException();
        }

        if (!registeredService.isSsoEnabled() && credentials == null
            && ticketGrantingTicket.getCountOfUses() > 0) {
            throw new UnauthorizedSsoServiceException();
        }

        if (credentials != null) {
            try {
                Authentication authentication = this.authenticationManager
                    .authenticate(credentials);

                final Principal originalPrincipal = ticketGrantingTicket
                    .getAuthentication().getPrincipal();
                final Principal newPrincipal = authentication.getPrincipal();

                if (!newPrincipal.equals(originalPrincipal)) {
                    throw new TicketCreationException();
                }
            } catch (final AuthenticationException e) {
                throw new TicketCreationException(e);
            }
        }

        final UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator = this.uniqueTicketIdGeneratorsForService
            .get(service.getClass().getName());

        final ServiceTicket serviceTicket = ticketGrantingTicket
            .grantServiceTicket(serviceTicketUniqueTicketIdGenerator
                .getNewTicketId(ServiceTicket.PREFIX), service,
                this.serviceTicketExpirationPolicy, credentials != null);

        this.ticketRegistry.addTicket(serviceTicket);

        if (log.isInfoEnabled()) {
            log.info("Granted service ticket ["
                + serviceTicket.getId()
                + "] for service ["
                + service.getId()
                + "] for user ["
                + serviceTicket.getGrantingTicket().getAuthentication()
                    .getPrincipal().getId() + "]");
        }

        return serviceTicket.getId();
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

        Assert.notNull(serviceTicketId, "serviceTicketId cannot be null");
        Assert.notNull(credentials, "credentials cannot be null");

        try {
            final Authentication authentication = this.authenticationManager
                .authenticate(credentials);

            final ServiceTicket serviceTicket;
            serviceTicket = (ServiceTicket) this.ticketRegistry.getTicket(
                serviceTicketId, ServiceTicket.class);

            if (serviceTicket == null || serviceTicket.isExpired()) {
                throw new InvalidTicketException();
            }

            final RegisteredService registeredService = this.servicesManager
                .findServiceBy(serviceTicket.getService());

            if (registeredService == null
                || !registeredService.isAllowedToProxy()) {
                throw new UnauthorizedProxyingException();
            }

            TicketGrantingTicket ticketGrantingTicket = serviceTicket
                .grantTicketGrantingTicket(
                    this.ticketGrantingTicketUniqueTicketIdGenerator
                        .getNewTicketId(TicketGrantingTicket.PREFIX),
                    authentication, this.ticketGrantingTicketExpirationPolicy);

            this.ticketRegistry.addTicket(ticketGrantingTicket);

            return ticketGrantingTicket.getId();
        } catch (final AuthenticationException e) {
            throw new TicketCreationException(e);
        }
    }

    /**
     * @throws IllegalArgumentException if the ServiceTicketId or the Service
     * are null.
     */
    public Assertion validateServiceTicket(final String serviceTicketId,
        final Service service) throws TicketException {
        Assert.notNull(serviceTicketId, "serviceTicketId cannot be null");
        Assert.notNull(service, "service cannot be null");

        final ServiceTicket serviceTicket = (ServiceTicket) this.ticketRegistry
            .getTicket(serviceTicketId, ServiceTicket.class);

        // TODO check for null
        final RegisteredService registeredService = this.servicesManager
            .findServiceBy(service);

        if (serviceTicket == null) {
            if (log.isDebugEnabled()) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] does not exist.");
            }
            throw new InvalidTicketException();
        }

        try {
            synchronized (serviceTicket) {
                if (serviceTicket.isExpired()) {
                    if (log.isDebugEnabled()) {
                        log.debug("ServiceTicket [" + serviceTicketId
                            + "] has expired.");
                    }
                    throw new InvalidTicketException();
                }

                if (!serviceTicket.isValidFor(service)) {
                    if (log.isDebugEnabled()) {
                        log.debug("ServiceTicket [" + serviceTicketId
                            + "] does not match supplied service: " + service);
                    }

                    throw new TicketValidationException();
                }
            }
            
            final Authentication authentication = serviceTicket.getGrantingTicket().getChainedAuthentications().get(serviceTicket.getGrantingTicket().getChainedAuthentications().size()-1);
            final Principal principal = authentication.getPrincipal();
            final String principalId = registeredService.isAnonymousAccess() ? "" : authentication.getPrincipal().getId();
            final Map<String, Object> attributes = new HashMap<String,Object>();
            
            for (final Attribute attribute : registeredService.getAllowedAttributes()) {
                final Object value = principal.getAttributes().get(attribute.getName());
                
                if (value != null) {
                    attributes.put(attribute.getName(), value);
                }
            }
            
            final Principal modifiedPrincipal = new SimplePrincipal(principalId, attributes);
            final MutableAuthentication mutableAuthentication = new MutableAuthentication(modifiedPrincipal);
            mutableAuthentication.getAttributes().putAll(authentication.getAttributes());
            mutableAuthentication.getAuthenticatedDate().setTime(authentication.getAuthenticatedDate().getTime());
            
            

            
            // TODO do service restrictions here
            // TODO pseudoanonymous
            
            return new ImmutableAssertionImpl(serviceTicket.getGrantingTicket()
                .getChainedAuthentications(), serviceTicket.getService(),
                serviceTicket.isFromNewLogin());
        } finally {
            if (serviceTicket.isExpired()) {
                this.ticketRegistry.deleteTicket(serviceTicketId);
            }
        }
    }

    /**
     * @throws IllegalArgumentException if the credentials are null.
     */
    public String createTicketGrantingTicket(final Credentials credentials)
        throws TicketCreationException {
        Assert.notNull(credentials, "credentials cannot be null");

        if (log.isDebugEnabled()) {
            log.debug("Attempting to create TicketGrantingTicket for "
                + credentials);
        }

        try {
            final Authentication authentication = this.authenticationManager
                .authenticate(credentials);

            final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
                this.ticketGrantingTicketUniqueTicketIdGenerator
                    .getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication, this.ticketGrantingTicketExpirationPolicy);

            this.ticketRegistry.addTicket(ticketGrantingTicket);

            return ticketGrantingTicket.getId();
        } catch (final AuthenticationException e) {
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

    public void setUniqueTicketIdGeneratorsForService(
        final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService) {
        this.uniqueTicketIdGeneratorsForService = uniqueTicketIdGeneratorsForService;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

}
