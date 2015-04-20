/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import com.github.inspektr.audit.annotation.Audit;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.*;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.services.UnauthorizedSsoServiceException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.TicketValidationException;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * <li> <code>serviceTicketRegistry</code> - Provides an alternative to configure separate registries for TGTs and ST in order to store them
 * in different locations (i.e. long term memory or short-term)</li>
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
 * @version $Revision: 1.16 $ $Date: 2007/04/24 18:11:36 $
 * @since 3.0
 */
public final class CentralAuthenticationServiceImpl implements CentralAuthenticationService {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /** TicketRegistry for storing and retrieving tickets as needed. */
    @NotNull
    private TicketRegistry ticketRegistry;

    /** New Ticket Registry for storing and retrieving services tickets. Can point to the same one as the ticketRegistry variable. */
    @NotNull
    private TicketRegistry serviceTicketRegistry;

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

    /** Implementation of Service Manager */
    @NotNull
    private ServicesManager servicesManager;

    /** Encoder to generate PseudoIds. */
    @NotNull
    private PersistentIdGenerator persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator();

    /**
     * Implementation of destoryTicketGrantingTicket expires the ticket provided
     * and removes it from the TicketRegistry.
     *
     * @throws IllegalArgumentException if the TicketGrantingTicket ID is null.
     */
    @Audit(
        action="TICKET_GRANTING_TICKET_DESTROYED",
        actionResolverName="DESTROY_TICKET_GRANTING_TICKET_RESOLVER",
        resourceResolverName="DESTROY_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Profiled(tag = "DESTROY_TICKET_GRANTING_TICKET",logFailuresSeparately = false)
    @Transactional(readOnly = false)
    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        Assert.notNull(ticketGrantingTicketId);

        if (log.isDebugEnabled()) {
            log.debug("Removing ticket [" + ticketGrantingTicketId + "] from registry.");
        }
        final TicketGrantingTicket ticket = (TicketGrantingTicket) this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        if (ticket == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Ticket found.  Expiring and then deleting.");
        }
        ticket.expire();
        this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
    }

    /**
     * @throws IllegalArgumentException if TicketGrantingTicket ID, Credentials
     * or Service are null.
     */
    @Audit(
        action="SERVICE_TICKET",
        actionResolverName="GRANT_SERVICE_TICKET_RESOLVER",
        resourceResolverName="GRANT_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Profiled(tag="GRANT_SERVICE_TICKET", logFailuresSeparately = false)
    @Transactional(readOnly = false)
    public String grantServiceTicket(final String ticketGrantingTicketId, final Service service, final Credentials credentials) throws TicketException {

        Assert.notNull(ticketGrantingTicketId, "ticketGrantingticketId cannot be null");
        Assert.notNull(service, "service cannot be null");

        final TicketGrantingTicket ticketGrantingTicket;
        ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

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

        if (registeredService == null || !registeredService.isEnabled()) {
            log.warn("ServiceManagement: Unauthorized Service Access. Service [" + service.getId() + "] not found in Service Registry.");
            throw new UnauthorizedServiceException();
        }

        if (!registeredService.isSsoEnabled() && credentials == null
            && ticketGrantingTicket.getCountOfUses() > 0) {
            log.warn("ServiceManagement: Service Not Allowed to use SSO.  Service [" + service.getId() + "]");
            throw new UnauthorizedSsoServiceException();
        }

        final String proxiedBy = ticketGrantingTicket.getProxiedBy();
        if(proxiedBy != null) {
            final RegisteredService proxyingService = servicesManager.findServiceBy(new SimpleWebApplicationServiceImpl(proxiedBy));
            if (proxyingService == null || !proxyingService.isAllowedToProxy()) {
                final String message = String.format("ServiceManagement: Service Attempted to Proxy, but is not allowed. Service: [%s] | Registered Service: [%s]", service.getId(), proxyingService);
                log.warn(message);
                throw new UnauthorizedProxyingException(message);
            }
        }

        if (credentials != null) {
            try {
                final Authentication authentication = this.authenticationManager
                    .authenticate(credentials);
                final Authentication originalAuthentication = ticketGrantingTicket.getAuthentication();

                if (!(authentication.getPrincipal().equals(originalAuthentication.getPrincipal()) && authentication.getAttributes().equals(originalAuthentication.getAttributes()))) {
                    throw new TicketCreationException();
                }
            } catch (final AuthenticationException e) {
                throw new TicketCreationException(e);
            }
        }

        // this code is a bit brittle by depending on the class name.  Future versions (i.e. CAS4 will know inherently how to identify themselves)
        final UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator = this.uniqueTicketIdGeneratorsForService
            .get(service.getClass().getName());

        final ServiceTicket serviceTicket = ticketGrantingTicket
            .grantServiceTicket(serviceTicketUniqueTicketIdGenerator
                .getNewTicketId(ServiceTicket.PREFIX), service,
                this.serviceTicketExpirationPolicy, credentials != null);

        this.serviceTicketRegistry.addTicket(serviceTicket);

        if (log.isInfoEnabled()) {
            final List<Authentication> authentications = serviceTicket.getGrantingTicket().getChainedAuthentications();
            final String formatString = "Granted %s ticket [%s] for service [%s] for user [%s]";
            final String type;
            final String principalId = authentications.get(authentications.size()-1).getPrincipal().getId();

            if (authentications.size() == 1) {
                type = "service";

            } else {
                type = "proxy";
            }

            log.info(String.format(formatString, type, serviceTicket.getId(), service.getId(), principalId));
        }

        return serviceTicket.getId();
    }

    @Audit(
        action="SERVICE_TICKET",
        actionResolverName="GRANT_SERVICE_TICKET_RESOLVER",
        resourceResolverName="GRANT_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Profiled(tag = "GRANT_SERVICE_TICKET",logFailuresSeparately = false)
    @Transactional(readOnly = false)
    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service) throws TicketException {
        return this.grantServiceTicket(ticketGrantingTicketId, service, null);
    }

    /**
     * @throws IllegalArgumentException if the ServiceTicketId or the
     * Credentials are null.
     */
    @Audit(
        action="PROXY_GRANTING_TICKET",
        actionResolverName="GRANT_PROXY_GRANTING_TICKET_RESOLVER",
        resourceResolverName="GRANT_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Profiled(tag="GRANT_PROXY_GRANTING_TICKET",logFailuresSeparately = false)
    @Transactional(readOnly = false)
    public String delegateTicketGrantingTicket(final String serviceTicketId,
        final Credentials credentials) throws TicketException {

        Assert.notNull(serviceTicketId, "serviceTicketId cannot be null");
        Assert.notNull(credentials, "credentials cannot be null");

        try {
            final ServiceTicket serviceTicket = (ServiceTicket) this.serviceTicketRegistry.getTicket(
                    serviceTicketId, ServiceTicket.class);

            if (serviceTicket == null || serviceTicket.isExpired()) {
                throw new InvalidTicketException();
            }

            final RegisteredService registeredService = this.servicesManager
                .findServiceBy(serviceTicket.getService());

            if (registeredService == null || !registeredService.isEnabled()
                || !registeredService.isAllowedToProxy()) {
                log.warn("ServiceManagement: Service Attempted to Proxy, but is not allowed.  Service: [" + serviceTicket.getService().getId() + "]");
                throw new UnauthorizedProxyingException();
            }

            final Authentication authentication = this.authenticationManager.authenticate(credentials);

            final TicketGrantingTicket ticketGrantingTicket = serviceTicket
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
    @Audit(
        action="SERVICE_TICKET_VALIDATE",
        actionResolverName="VALIDATE_SERVICE_TICKET_RESOLVER",
        resourceResolverName="VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Profiled(tag="VALIDATE_SERVICE_TICKET",logFailuresSeparately = false)
    @Transactional(readOnly = false)
    public Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws TicketException {
        Assert.notNull(serviceTicketId, "serviceTicketId cannot be null");
        Assert.notNull(service, "service cannot be null");

        final ServiceTicket serviceTicket = (ServiceTicket) this.serviceTicketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        if (registeredService == null || !registeredService.isEnabled()) {
            log.warn("ServiceManagement: Service does not exist is not enabled, and thus not allowed to validate tickets.   Service: [" + service.getId() + "]");
            throw new UnauthorizedServiceException("Service not allowed to validate tickets.");
        }

        if (serviceTicket == null) {
            log.info("ServiceTicket [" + serviceTicketId + "] does not exist.");
            throw new InvalidTicketException();
        }

        try {
            synchronized (serviceTicket) {
                if (serviceTicket.isExpired()) {
                    log.info("ServiceTicket [" + serviceTicketId + "] has expired.");
                    throw new InvalidTicketException();
                }

                if (!serviceTicket.isValidFor(service)) {
                    log.error("ServiceTicket [" + serviceTicketId + "] with service [" + serviceTicket.getService().getId() + " does not match supplied service [" + service + "]");
                    throw new TicketValidationException(serviceTicket.getService());
                }
            }

            final List<Authentication> chainedAuthenticationsList = serviceTicket.getGrantingTicket().getChainedAuthentications();
            final Authentication authentication = chainedAuthenticationsList.get(chainedAuthenticationsList.size() - 1);
            final Principal principal = authentication.getPrincipal();
           
            final String principalId = determinePrincipalIdForRegisteredService(principal, registeredService, serviceTicket);
            final Authentication authToUse;

            if (!registeredService.isIgnoreAttributes()) {
                final Map<String, Object> attributes = new HashMap<String, Object>();

                for (final String attribute : registeredService.getAllowedAttributes()) {
                    final Object value = principal.getAttributes().get(attribute);

                    if (value != null) {
                        attributes.put(attribute, value);
                    }
                }

                final Principal modifiedPrincipal = new SimplePrincipal(principalId, attributes);
                final MutableAuthentication mutableAuthentication = new MutableAuthentication(
                    modifiedPrincipal, authentication.getAuthenticatedDate());
                mutableAuthentication.getAttributes().putAll(
                    authentication.getAttributes());
                mutableAuthentication.getAuthenticatedDate().setTime(
                    authentication.getAuthenticatedDate().getTime());
                authToUse = mutableAuthentication;
            } else {
                final Principal modifiedPrincipal = new SimplePrincipal(principalId, principal.getAttributes());
                authToUse = new MutableAuthentication(modifiedPrincipal, authentication.getAuthenticatedDate());
            }
           
            final List<Authentication> authentications = new ArrayList<Authentication>();

            for (int i = 0; i < chainedAuthenticationsList.size() - 1; i++) {
                authentications.add(serviceTicket.getGrantingTicket().getChainedAuthentications().get(i));
            }
            authentications.add(authToUse);

            return new ImmutableAssertionImpl(authentications, serviceTicket.getService(), serviceTicket.isFromNewLogin());
        } finally {
            if (serviceTicket.isExpired()) {
                this.serviceTicketRegistry.deleteTicket(serviceTicketId);
            }
        }
    }

    /**
     * Determines the principal id to use for a {@link RegisteredService} using the following rules: 
     * 
     * <ul>
     *  <li> If the service is marked to allow anonymous access, a persistent id is returned. </li>
     *  <li> If the attribute name matches {@link RegisteredService#DEFAULT_USERNAME_ATTRIBUTE}, then the default principal id is returned.</li>
     *  <li>If the service is set to ignore attributes, or the username attribute exists in the allowed attributes for the service, 
     *      the corresponding attribute value will be returned.
     *  </li>
     *   <li>Otherwise, the default principal's id is returned as the username attribute with an additional warning.</li>
     * </ul>
     * 
     * @param principal The principal object to be validated and constructed
     * @param registeredService Requesting service for which a principal is being validated. 
     * @param serviceTicket An instance of the service ticket used for validation
     * 
     * @return The principal id to use for the requesting registered service
     */
    private String determinePrincipalIdForRegisteredService(final Principal principal, final RegisteredService registeredService, 
                                                            final ServiceTicket serviceTicket) {
        String principalId = null;
        final String serviceUsernameAttribute = registeredService.getUsernameAttribute();

        if (registeredService.isAnonymousAccess()) {
            principalId = this.persistentIdGenerator.generate(principal, serviceTicket.getService());
        } else if (StringUtils.isBlank(serviceUsernameAttribute)) {
            principalId = principal.getId();
        } else {
            if (principal.getAttributes().containsKey(serviceUsernameAttribute)) {
                principalId = principal.getAttributes().get(registeredService.getUsernameAttribute()).toString();
            } else {
                principalId = principal.getId();
                final Object[] errorLogParameters = new Object[] { principalId, registeredService.getUsernameAttribute(),
                        principal.getAttributes(), registeredService.getServiceId(), principalId };
                log.warn("Principal [{}] did not have attribute [{}] among attributes [{}] so CAS cannot "
                        + "provide on the validation response the user attribute the registered service [{}] expects. "
                        + "CAS will instead return the default username attribute [{}]", errorLogParameters);
            }

        }
        
        log.debug("Principal id to return for service [{}] is [{}]. The default principal id is [{}].", 
                  new Object[] {registeredService.getName(), principal.getId(), principalId});
        return principalId;
    }

    /**
     * @throws IllegalArgumentException if the credentials are null.
     */
    @Audit(
        action="TICKET_GRANTING_TICKET",
        actionResolverName="CREATE_TICKET_GRANTING_TICKET_RESOLVER",
        resourceResolverName="CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Profiled(tag = "CREATE_TICKET_GRANTING_TICKET", logFailuresSeparately = false)
    @Transactional(readOnly = false)
    public String createTicketGrantingTicket(final Credentials credentials) throws TicketCreationException {
        Assert.notNull(credentials, "credentials cannot be null");

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

        if (this.serviceTicketRegistry == null) {
            this.serviceTicketRegistry = ticketRegistry;
        }
    }

    public void setServiceTicketRegistry(final TicketRegistry serviceTicketRegistry) {
        this.serviceTicketRegistry = serviceTicketRegistry;
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

    public void setPersistentIdGenerator(
        final PersistentIdGenerator persistentIdGenerator) {
        this.persistentIdGenerator = persistentIdGenerator;
    }
}
