/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.github.inspektr.audit.annotation.Audit;
import org.apache.commons.collections4.Predicate;
import org.jasig.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.ContextualAuthenticationPolicy;
import org.jasig.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.MixedPrincipalException;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PersistentIdGenerator;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.services.AttributeReleasePolicy;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceContext;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.services.UnauthorizedServiceForPrincipalException;
import org.jasig.cas.services.UnauthorizedSsoServiceException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.UnrecognizableServiceForServiceTicketValidationException;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * <li> <code>serviceTicketRegistry</code> - Provides an alternative to configure separate registries for
 * TGTs and ST in order to store them in different locations (i.e. long term memory or short-term)</li>
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
 * @since 3.0.0
 */
public final class CentralAuthenticationServiceImpl implements CentralAuthenticationService {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** TicketRegistry for storing and retrieving tickets as needed. */
    @NotNull
    private final TicketRegistry ticketRegistry;

    /** New Ticket Registry for storing and retrieving services tickets. Can point to the same one as the ticketRegistry variable. */
    @NotNull
    private final TicketRegistry serviceTicketRegistry;

    /**
     * AuthenticationManager for authenticating credentials for purposes of
     * obtaining tickets.
     */
    @NotNull
    private final AuthenticationManager authenticationManager;

    /**
     * UniqueTicketIdGenerator to generate ids for TicketGrantingTickets
     * created.
     */
    @NotNull
    private final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Map to contain the mappings of service->UniqueTicketIdGenerators. */
    @NotNull
    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    /** Implementation of Service Manager. */
    @NotNull
    private final ServicesManager servicesManager;

    /** The logout manager. **/
    @NotNull
    private final LogoutManager logoutManager;

    /** Expiration policy for ticket granting tickets. */
    @NotNull
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets. */
    @NotNull
    private ExpirationPolicy serviceTicketExpirationPolicy;

    /**
     * Authentication policy that uses a service context to produce stateful security policies to apply when
     * authenticating credentials.
     */
    @NotNull
    private ContextualAuthenticationPolicyFactory<ServiceContext> serviceContextAuthenticationPolicyFactory =
            new AcceptAnyAuthenticationPolicyFactory();

    /** Factory to create the principal type. **/
    @NotNull
    private PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /** Default instance for the ticket id generator. */
    @NotNull
    private final UniqueTicketIdGenerator defaultServiceTicketIdGenerator
            = new DefaultUniqueTicketIdGenerator();
    /**
     * Build the central authentication service implementation.
     *
     * @param ticketRegistry the tickets registry.
     * @param serviceTicketRegistry the service tickets registry.
     * @param authenticationManager the authentication manager.
     * @param ticketGrantingTicketUniqueTicketIdGenerator the TGT id generator.
     * @param uniqueTicketIdGeneratorsForService the map with service and ticket id generators.
     * @param ticketGrantingTicketExpirationPolicy the TGT expiration policy.
     * @param serviceTicketExpirationPolicy the service ticket expiration policy.
     * @param servicesManager the services manager.
     * @param logoutManager the logout manager.
     */
    public CentralAuthenticationServiceImpl(final TicketRegistry ticketRegistry,
                                            final TicketRegistry serviceTicketRegistry,
                                            final AuthenticationManager authenticationManager,
                                            final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                                            final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService,
                                            final ExpirationPolicy ticketGrantingTicketExpirationPolicy,
                                            final ExpirationPolicy serviceTicketExpirationPolicy,
                                            final ServicesManager servicesManager,
                                            final LogoutManager logoutManager) {
        this.ticketRegistry = ticketRegistry;
        if (serviceTicketRegistry == null) {
            this.serviceTicketRegistry = ticketRegistry;
        } else {
            this.serviceTicketRegistry = serviceTicketRegistry;
        }
        this.authenticationManager = authenticationManager;
        this.ticketGrantingTicketUniqueTicketIdGenerator = ticketGrantingTicketUniqueTicketIdGenerator;
        this.uniqueTicketIdGeneratorsForService = uniqueTicketIdGeneratorsForService;
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
        this.servicesManager = servicesManager;
        this.logoutManager = logoutManager;
    }

    /**
     * {@inheritDoc}
     * Destroy a TicketGrantingTicket and perform back channel logout. This has the effect of invalidating any
     * Ticket that was derived from the TicketGrantingTicket being destroyed. May throw an
     * {@link IllegalArgumentException} if the TicketGrantingTicket ID is null.
     *
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     * @return the logout requests.
     */
    @Audit(
            action="TICKET_GRANTING_TICKET_DESTROYED",
            actionResolverName="DESTROY_TICKET_GRANTING_TICKET_RESOLVER",
            resourceResolverName="DESTROY_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Timed(name = "DESTROY_TICKET_GRANTING_TICKET_TIMER")
    @Metered(name="DESTROY_TICKET_GRANTING_TICKET_METER")
    @Counted(name="DESTROY_TICKET_GRANTING_TICKET_COUNTER", monotonic=true)
    @Transactional(readOnly = false)
    @Override
    public List<LogoutRequest> destroyTicketGrantingTicket(@NotNull final String ticketGrantingTicketId) {
        try {
            logger.debug("Removing ticket [{}] from registry...", ticketGrantingTicketId);
            final TicketGrantingTicket ticket = getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            logger.debug("Ticket found. Processing logout requests and then deleting the ticket...");
            final List<LogoutRequest> logoutRequests = logoutManager.performLogout(ticket);
            this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
            return logoutRequests;
        } catch (final InvalidTicketException e) {
            logger.debug("TicketGrantingTicket [{}] cannot be found in the ticket registry.", ticketGrantingTicketId);
        }
        return Collections.emptyList();
    }

    @Audit(
        action="SERVICE_TICKET",
        actionResolverName="GRANT_SERVICE_TICKET_RESOLVER",
        resourceResolverName="GRANT_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Timed(name="GRANT_SERVICE_TICKET_TIMER")
    @Metered(name="GRANT_SERVICE_TICKET_METER")
    @Counted(name="GRANT_SERVICE_TICKET_COUNTER", monotonic=true)
    @Transactional(readOnly = false)
    @Override
    public ServiceTicket grantServiceTicket(
            final String ticketGrantingTicketId,
            final Service service, final Credential... credentials)
            throws AuthenticationException, TicketException {

        final TicketGrantingTicket ticketGrantingTicket = getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        verifyRegisteredServiceProperties(registeredService, service);
        final Set<Credential> sanitizedCredentials = sanitizeCredentials(credentials);

        Authentication currentAuthentication = null;
        if (sanitizedCredentials.size() > 0) {
            currentAuthentication = this.authenticationManager.authenticate(
                    sanitizedCredentials.toArray(new Credential[] {}));
            final Authentication original = ticketGrantingTicket.getAuthentication();
            if (!currentAuthentication.getPrincipal().equals(original.getPrincipal())) {
                throw new MixedPrincipalException(
                        currentAuthentication, currentAuthentication.getPrincipal(), original.getPrincipal());
            }
            ticketGrantingTicket.getSupplementalAuthentications().add(currentAuthentication);
        }

        if (currentAuthentication == null && !registeredService.getAccessStrategy().isServiceAccessAllowedForSso()) {
            logger.warn("ServiceManagement: Service [{}] is not allowed to use SSO.", service.getId());
            throw new UnauthorizedSsoServiceException();
        }

        final Service proxiedBy = ticketGrantingTicket.getProxiedBy();
        if (proxiedBy != null) {
            logger.debug("TGT is proxied by [{}]. Locating proxy service in registry...", proxiedBy.getId());
            final RegisteredService proxyingService = servicesManager.findServiceBy(proxiedBy);

            if (proxyingService != null) {
                logger.debug("Located proxying service [{}] in the service registry", proxyingService);
                if (!proxyingService.getProxyPolicy().isAllowedToProxy()) {
                    logger.warn("Found proxying service {}, but it is not authorized to fulfill the proxy attempt made by {}",
                            proxyingService.getId(), service.getId());
                    throw new UnauthorizedProxyingException("Proxying is not allowed for registered service "
                            + registeredService.getId());
                }
            } else {
                logger.warn("No proxying service found. Proxy attempt by service [{}] (registered service [{}]) is not allowed.",
                        service.getId(), registeredService.getId());
                throw new UnauthorizedProxyingException("Proxying is not allowed for registered service "
                        + registeredService.getId());
            }
        } else {
            logger.trace("TGT is not proxied by another service");
        }

        // Perform security policy check by getting the authentication that satisfies the configured policy
        // This throws if no suitable policy is found
        getAuthenticationSatisfiedByPolicy(ticketGrantingTicket, new ServiceContext(service, registeredService));

        final List<Authentication> authentications = ticketGrantingTicket.getChainedAuthentications();
        final Principal principal = authentications.get(authentications.size() - 1).getPrincipal();

        final Map<String, Object> principalAttrs = registeredService.getAttributeReleasePolicy().getAttributes(principal);
        if (!registeredService.getAccessStrategy().doPrincipalAttributesAllowServiceAccess(principalAttrs)) {
            logger.warn("ServiceManagement: Cannot grant service ticket because Service [{}] is not authorized for use by [{}].",
                    service.getId(), principal);
            throw new UnauthorizedServiceForPrincipalException();
        }

        final String uniqueTicketIdGenKey = service.getClass().getName();
        logger.debug("Looking up service ticket id generator for [{}]", uniqueTicketIdGenKey);
        UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator =
                this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        if (serviceTicketUniqueTicketIdGenerator == null) {
            serviceTicketUniqueTicketIdGenerator = this.defaultServiceTicketIdGenerator;
            logger.debug("Service ticket id generator not found for [{}]. Using the default generator...",
                    uniqueTicketIdGenKey);
        }

        final String ticketPrefix = authentications.size() == 1 ? ServiceTicket.PREFIX : ServiceTicket.PROXY_TICKET_PREFIX;
        final String ticketId = serviceTicketUniqueTicketIdGenerator.getNewTicketId(ticketPrefix);
        final ServiceTicket serviceTicket = ticketGrantingTicket.grantServiceTicket(
                ticketId,
                service,
                this.serviceTicketExpirationPolicy,
                currentAuthentication != null);

        this.serviceTicketRegistry.addTicket(serviceTicket);

        logger.info("Granted ticket [{}] for service [{}] for user [{}]",
                serviceTicket.getId(), service.getId(), principal.getId());

        return serviceTicket;
    }

    /**
     * Attempts to sanitize the array of credentials by removing
     * all null elements from the collection.
     * @param credentials credentials to sanitize
     * @return a set of credentials with no null values
     */
    private static Set<Credential> sanitizeCredentials(final Credential[] credentials) {
        if (credentials != null && credentials.length > 0) {
            final Set<Credential> set = new HashSet<>(Arrays.asList(credentials));
            final Iterator<Credential> it = set.iterator();
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                }
            }
            return set;
        }
        return Collections.emptySet();
    }

    @Audit(
        action="SERVICE_TICKET",
        actionResolverName="GRANT_SERVICE_TICKET_RESOLVER",
        resourceResolverName="GRANT_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Timed(name = "GRANT_SERVICE_TICKET_TIMER")
    @Metered(name="GRANT_SERVICE_TICKET_METER")
    @Counted(name="GRANT_SERVICE_TICKET_COUNTER", monotonic=true)
    @Transactional(readOnly = false)
    @Override
    public ServiceTicket grantServiceTicket(final String ticketGrantingTicketId,
        final Service service) throws TicketException {
        try {
            return this.grantServiceTicket(ticketGrantingTicketId, service, (Credential[]) null);
        } catch (final AuthenticationException e) {
            throw new IllegalStateException("Unexpected authentication exception", e);
        }
    }

    @Audit(
        action="PROXY_GRANTING_TICKET",
        actionResolverName="GRANT_PROXY_GRANTING_TICKET_RESOLVER",
        resourceResolverName="GRANT_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Timed(name="GRANT_PROXY_GRANTING_TICKET_TIMER")
    @Metered(name="GRANT_PROXY_GRANTING_TICKET_METER")
    @Counted(name="GRANT_PROXY_GRANTING_TICKET_COUNTER", monotonic=true)
    @Transactional(readOnly = false)
    @Override
    public TicketGrantingTicket delegateTicketGrantingTicket(final String serviceTicketId, final Credential... credentials)
            throws AuthenticationException, TicketException {

        final ServiceTicket serviceTicket =  this.serviceTicketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

        if (serviceTicket == null || serviceTicket.isExpired()) {
            logger.debug("ServiceTicket [{}] has expired or cannot be found in the ticket registry", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }

        final RegisteredService registeredService = this.servicesManager
                .findServiceBy(serviceTicket.getService());

        verifyRegisteredServiceProperties(registeredService, serviceTicket.getService());
        
        if (!registeredService.getProxyPolicy().isAllowedToProxy()) {
            logger.warn("ServiceManagement: Service [{}] attempted to proxy, but is not allowed.", serviceTicket.getService().getId());
            throw new UnauthorizedProxyingException();
        }

        final Authentication authentication = this.authenticationManager.authenticate(credentials);

        final String pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(
                TicketGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        final TicketGrantingTicket proxyGrantingTicket = serviceTicket.grantTicketGrantingTicket(pgtId,
                                    authentication, this.ticketGrantingTicketExpirationPolicy);

        logger.debug("Generated proxy granting ticket [{}] based off of [{}]", proxyGrantingTicket, serviceTicketId);
        this.ticketRegistry.addTicket(proxyGrantingTicket);

        return proxyGrantingTicket;
    }

    @Audit(
        action="SERVICE_TICKET_VALIDATE",
        actionResolverName="VALIDATE_SERVICE_TICKET_RESOLVER",
        resourceResolverName="VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Timed(name="VALIDATE_SERVICE_TICKET_TIMER")
    @Metered(name="VALIDATE_SERVICE_TICKET_METER")
    @Counted(name="VALIDATE_SERVICE_TICKET_COUNTER", monotonic=true)
    @Transactional(readOnly = false)
    @Override
    public Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws TicketException {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        verifyRegisteredServiceProperties(registeredService, service);

        final ServiceTicket serviceTicket =  this.serviceTicketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

        if (serviceTicket == null) {
            logger.info("Service ticket [{}] does not exist.", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }

        try {
            synchronized (serviceTicket) {
                if (serviceTicket.isExpired()) {
                    logger.info("ServiceTicket [{}] has expired.", serviceTicketId);
                    throw new InvalidTicketException(serviceTicketId);
                }

                if (!serviceTicket.isValidFor(service)) {
                    logger.error("Service ticket [{}] with service [{}] does not match supplied service [{}]",
                            serviceTicketId, serviceTicket.getService().getId(), service);
                    throw new UnrecognizableServiceForServiceTicketValidationException(serviceTicket.getService());
                }
            }

            final TicketGrantingTicket root = serviceTicket.getGrantingTicket().getRoot();
            final Authentication authentication = getAuthenticationSatisfiedByPolicy(
                    root, new ServiceContext(serviceTicket.getService(), registeredService));
            final Principal principal = authentication.getPrincipal();

            final AttributeReleasePolicy attributePolicy = registeredService.getAttributeReleasePolicy();
            logger.debug("Attribute policy [{}] is associated with service [{}]", attributePolicy, registeredService);
            
            @SuppressWarnings("unchecked")
            final Map<String, Object> attributesToRelease = attributePolicy != null
                    ? attributePolicy.getAttributes(principal) : Collections.EMPTY_MAP;
            
            final String principalId = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service);
            final Principal modifiedPrincipal = this.principalFactory.createPrincipal(principalId, attributesToRelease);
            final AuthenticationBuilder builder = AuthenticationBuilder.newInstance(authentication);
            builder.setPrincipal(modifiedPrincipal);

            return new ImmutableAssertion(
                    builder.build(),
                    serviceTicket.getGrantingTicket().getChainedAuthentications(),
                    serviceTicket.getService(),
                    serviceTicket.isFromNewLogin());
        } finally {
            if (serviceTicket.isExpired()) {
                this.serviceTicketRegistry.deleteTicket(serviceTicketId);
            }
        }
    }
    
    @Audit(
        action="TICKET_GRANTING_TICKET",
        actionResolverName="CREATE_TICKET_GRANTING_TICKET_RESOLVER",
        resourceResolverName="CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Timed(name = "CREATE_TICKET_GRANTING_TICKET_TIMER")
    @Metered(name = "CREATE_TICKET_GRANTING_TICKET_METER")
    @Counted(name="CREATE_TICKET_GRANTING_TICKET_COUNTER", monotonic=true)
    @Transactional(readOnly = false)
    @Override
    public TicketGrantingTicket createTicketGrantingTicket(final Credential... credentials)
            throws AuthenticationException, TicketException {

        final Set<Credential> sanitizedCredentials = sanitizeCredentials(credentials);
        if (sanitizedCredentials.size() > 0) {
            final Authentication authentication = this.authenticationManager.authenticate(credentials);

            final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
                    this.ticketGrantingTicketUniqueTicketIdGenerator
                            .getNewTicketId(TicketGrantingTicket.PREFIX),
                    authentication, this.ticketGrantingTicketExpirationPolicy);

            this.ticketRegistry.addTicket(ticketGrantingTicket);
            return ticketGrantingTicket;
        }
        final String msg = "No credentials were specified in the request for creating a new ticket-granting ticket";
        logger.warn(msg);
        throw new TicketCreationException(new IllegalArgumentException(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Timed(name = "GET_TICKET_TIMER")
    @Metered(name = "GET_TICKET_METER")
    @Counted(name="GET_TICKET_COUNTER", monotonic=true)
    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<? extends Ticket> clazz)
            throws InvalidTicketException {
        Assert.notNull(ticketId, "ticketId cannot be null");
        final Ticket ticket = this.ticketRegistry.getTicket(ticketId, clazz);

        if (ticket == null) {
            logger.debug("Ticket [{}] by type [{}] cannot be found in the ticket registry.", ticketId, clazz.getSimpleName());
            throw new InvalidTicketException(ticketId);
        }

        if (ticket instanceof TicketGrantingTicket) {
            synchronized (ticket) {
                if (ticket.isExpired()) {
                    this.ticketRegistry.deleteTicket(ticketId);
                    logger.debug("Ticket [{}] has expired and is now deleted from the ticket registry.", ticketId);
                    throw new InvalidTicketException(ticketId);
                }
            }
        }
        return (T) ticket;
    }
    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Timed(name = "GET_TICKETS_TIMER")
    @Metered(name = "GET_TICKETS_METER")
    @Counted(name="GET_TICKETS_COUNTER", monotonic=true)
    @Override
    public Collection<Ticket> getTickets(final Predicate predicate) {
        final Collection<Ticket> c = new HashSet<>(this.ticketRegistry.getTickets());
        final Iterator<Ticket> it = c.iterator();
        while (it.hasNext()) {
            if (!predicate.evaluate(it.next())) {
                it.remove();
            }
        }
        return c;
    }

    public void setServiceContextAuthenticationPolicyFactory(final ContextualAuthenticationPolicyFactory<ServiceContext> policy) {
        this.serviceContextAuthenticationPolicyFactory = policy;
    }

    /**
     * @param ticketGrantingTicketExpirationPolicy a TGT expiration policy.
     */
    public void setTicketGrantingTicketExpirationPolicy(final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
    }

    /**
     * @param serviceTicketExpirationPolicy a ST expiration policy.
     */
    public void setServiceTicketExpirationPolicy(final ExpirationPolicy serviceTicketExpirationPolicy) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
    }

    /**
     * @deprecated
     * Sets persistent id generator.
     *
     * @param persistentIdGenerator the persistent id generator
     */
    @Deprecated
    public void setPersistentIdGenerator(final PersistentIdGenerator persistentIdGenerator) {
        logger.warn("setPersistentIdGenerator() is deprecated and no longer available. Consider "
                + "configuring the an attribute provider for service definitions.");
    }

   /**
    * Sets principal factory to create principal objects.
    *
    * @param principalFactory the principal factory
    */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

   
    /**
     * Gets the authentication satisfied by policy.
     *
     * @param ticket the ticket
     * @param context the context
     * @return the authentication satisfied by policy
     * @throws org.jasig.cas.ticket.TicketException the ticket exception
     */
    private Authentication getAuthenticationSatisfiedByPolicy(
            final TicketGrantingTicket ticket, final ServiceContext context) throws TicketException {

        final ContextualAuthenticationPolicy<ServiceContext> policy =
                serviceContextAuthenticationPolicyFactory.createPolicy(context);
        if (policy.isSatisfiedBy(ticket.getAuthentication())) {
            return ticket.getAuthentication();
        }
        for (final Authentication auth : ticket.getSupplementalAuthentications()) {
            if (policy.isSatisfiedBy(auth)) {
                return auth;
            }
        }
        throw new UnsatisfiedAuthenticationPolicyException(policy);
    }
    
    /**
     * Ensure that the service is found and enabled in the service registry.
     * @param registeredService the located entry in the registry
     * @param service authenticating service
     * @throws UnauthorizedServiceException
     */
    private void verifyRegisteredServiceProperties(final RegisteredService registeredService, final Service service) {
        if (registeredService == null) {
            final String msg = String.format("ServiceManagement: Unauthorized Service Access. "
                    + "Service [%s] is not found in service registry.", service.getId());
            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            final String msg = String.format("ServiceManagement: Unauthorized Service Access. "
                    + "Service [%s] is not enabled in service registry.", service.getId());

            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
    }
}
