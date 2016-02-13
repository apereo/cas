package org.jasig.cas;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.MixedPrincipalException;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.ServiceContext;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceForPrincipalException;
import org.jasig.cas.services.UnauthorizedSsoServiceException;
import org.jasig.cas.support.events.CasProxyGrantingTicketCreatedEvent;
import org.jasig.cas.support.events.CasProxyTicketGrantedEvent;
import org.jasig.cas.support.events.CasServiceTicketGrantedEvent;
import org.jasig.cas.support.events.CasServiceTicketValidatedEvent;
import org.jasig.cas.support.events.CasTicketGrantingTicketCreatedEvent;
import org.jasig.cas.support.events.CasTicketGrantingTicketDestroyedEvent;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketFactory;
import org.jasig.cas.ticket.TicketFactory;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketFactory;
import org.jasig.cas.ticket.UnrecognizableServiceForServiceTicketValidationException;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.ticket.proxy.ProxyTicketFactory;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertion;
import org.jasig.inspektr.audit.annotation.Audit;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Concrete implementation of a {@link CentralAuthenticationService}, and also the
 * central, organizing component of CAS's internal implementation.
 * This class is threadsafe.
 *
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @author Dmitry Kopylenko
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Component("centralAuthenticationService")
public final class CentralAuthenticationServiceImpl extends AbstractCentralAuthenticationService {

    private static final long serialVersionUID = -8943828074939533986L;

    /**
     * Instantiates a new Central authentication service impl.
     */
    public CentralAuthenticationServiceImpl() {
        super();
    }

    /**
     * Build the central authentication service implementation.
     *
     * @param ticketRegistry                              the tickets registry.
     * @param ticketFactory                               the ticket factory
     * @param servicesManager                             the services manager.
     * @param logoutManager                               the logout manager.
     */
    public CentralAuthenticationServiceImpl(
            final TicketRegistry ticketRegistry,
            final TicketFactory ticketFactory,
            final ServicesManager servicesManager,
            final LogoutManager logoutManager) {

        super(ticketRegistry, ticketFactory, servicesManager, logoutManager);
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
    @Override
    public List<LogoutRequest> destroyTicketGrantingTicket(@NotNull final String ticketGrantingTicketId) {
        try {
            logger.debug("Removing ticket [{}] from registry...", ticketGrantingTicketId);
            final TicketGrantingTicket ticket = getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            logger.debug("Ticket found. Processing logout requests and then deleting the ticket...");
            final List<LogoutRequest> logoutRequests = logoutManager.performLogout(ticket);
            this.ticketRegistry.deleteTicket(ticketGrantingTicketId);

            doPublishEvent(new CasTicketGrantingTicketDestroyedEvent(this, ticket));

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
    @Override
    public ServiceTicket grantServiceTicket(
            final String ticketGrantingTicketId,
            final Service service, final AuthenticationContext context)
            throws AuthenticationException, AbstractTicketException {

        final TicketGrantingTicket ticketGrantingTicket = getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        verifyRegisteredServiceProperties(registeredService, service);
        final Authentication currentAuthentication = evaluatePossibilityOfMixedPrincipals(context, ticketGrantingTicket);

        if (currentAuthentication == null && !registeredService.getAccessStrategy().isServiceAccessAllowedForSso()) {
            logger.warn("Service [{}] is not allowed to use SSO.", service.getId());
            throw new UnauthorizedSsoServiceException();
        }

        evaluateProxiedServiceIfNeeded(service, ticketGrantingTicket, registeredService);

        // Perform security policy check by getting the authentication that satisfies the configured policy
        // This throws if no suitable policy is found
        getAuthenticationSatisfiedByPolicy(ticketGrantingTicket.getRoot(), new ServiceContext(service, registeredService));

        final List<Authentication> authentications = ticketGrantingTicket.getChainedAuthentications();
        final Principal principal = authentications.get(authentications.size() - 1).getPrincipal();

        final RegisteredServiceAttributeReleasePolicy releasePolicy = registeredService.getAttributeReleasePolicy();
        final Map<String, Object> principalAttrs;
        if (releasePolicy != null) {
            principalAttrs = releasePolicy.getAttributes(principal);
        } else {
            principalAttrs = new HashMap<>();
        }

        if (!registeredService.getAccessStrategy().doPrincipalAttributesAllowServiceAccess(principal.getId(), principalAttrs)) {
            logger.warn("Cannot grant service ticket because Service [{}] is not authorized for use by [{}].",
                    service.getId(), principal);
            throw new UnauthorizedServiceForPrincipalException();
        }

        final ServiceTicketFactory factory = this.ticketFactory.get(ServiceTicket.class);
        final ServiceTicket serviceTicket = factory.create(ticketGrantingTicket, service, currentAuthentication != null);
        this.ticketRegistry.addTicket(serviceTicket);

        logger.info("Granted ticket [{}] for service [{}] and principal [{}]",
                serviceTicket.getId(), service.getId(), principal.getId());

        doPublishEvent(new CasServiceTicketGrantedEvent(this, ticketGrantingTicket, serviceTicket));

        return serviceTicket;
    }

    private static Authentication evaluatePossibilityOfMixedPrincipals(final AuthenticationContext context,
                                                                final TicketGrantingTicket ticketGrantingTicket)
            throws MixedPrincipalException {
        Authentication currentAuthentication = null;
        if (context != null) {
            currentAuthentication = context.getAuthentication();
            if (currentAuthentication != null) {
                final Authentication original = ticketGrantingTicket.getAuthentication();
                if (!currentAuthentication.getPrincipal().equals(original.getPrincipal())) {
                    throw new MixedPrincipalException(
                            currentAuthentication, currentAuthentication.getPrincipal(), original.getPrincipal());
                }
                ticketGrantingTicket.getSupplementalAuthentications().add(currentAuthentication);
            }
        }
        return currentAuthentication;
    }

    @Audit(
            action="PROXY_TICKET",
            actionResolverName="GRANT_PROXY_TICKET_RESOLVER",
            resourceResolverName="GRANT_PROXY_TICKET_RESOURCE_RESOLVER")
    @Timed(name="GRANT_PROXY_TICKET_TIMER")
    @Metered(name="GRANT_PROXY_TICKET_METER")
    @Counted(name="GRANT_PROXY_TICKET_COUNTER", monotonic=true)
    @Override
    public ProxyTicket grantProxyTicket(final String proxyGrantingTicket, final Service service)
            throws AbstractTicketException {

        final ProxyGrantingTicket proxyGrantingTicketObject = getTicket(proxyGrantingTicket, ProxyGrantingTicket.class);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        verifyRegisteredServiceProperties(registeredService, service);

        if (proxyGrantingTicketObject.getAuthentication() == null
                && !registeredService.getAccessStrategy().isServiceAccessAllowedForSso()) {
            logger.warn("Service [{}] is not allowed to use SSO.", service.getId());
            throw new UnauthorizedSsoServiceException();
        }

        evaluateProxiedServiceIfNeeded(service, proxyGrantingTicketObject, registeredService);

        // Perform security policy check by getting the authentication that satisfies the configured policy
        // This throws if no suitable policy is found
        getAuthenticationSatisfiedByPolicy(proxyGrantingTicketObject.getRoot(), new ServiceContext(service, registeredService));

        final List<Authentication> authentications = proxyGrantingTicketObject.getChainedAuthentications();
        final Principal principal = authentications.get(authentications.size() - 1).getPrincipal();

        final RegisteredServiceAttributeReleasePolicy releasePolicy = registeredService.getAttributeReleasePolicy();
        final Map<String, Object> principalAttrs;
        if (releasePolicy != null) {
            principalAttrs = releasePolicy.getAttributes(principal);
        } else {
            principalAttrs = new HashMap<>();
        }

        if (!registeredService.getAccessStrategy().doPrincipalAttributesAllowServiceAccess(principal.getId(), principalAttrs)) {
            logger.warn("Cannot grant proxy ticket because Service [{}] is not authorized for use by [{}].",
                    service.getId(), principal);
            throw new UnauthorizedServiceForPrincipalException();
        }

        final ProxyTicketFactory factory = this.ticketFactory.get(ProxyTicket.class);
        final ProxyTicket proxyTicket = factory.create(proxyGrantingTicketObject, service);
        this.ticketRegistry.addTicket(proxyTicket);

        logger.info("Granted ticket [{}] for service [{}] for user [{}]",
                proxyTicket.getId(), service.getId(), principal.getId());

        doPublishEvent(new CasProxyTicketGrantedEvent(this, proxyGrantingTicketObject, proxyTicket));
        return proxyTicket;
    }

    @Audit(
            action="PROXY_GRANTING_TICKET",
            actionResolverName="CREATE_PROXY_GRANTING_TICKET_RESOLVER",
            resourceResolverName="CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Timed(name = "CREATE_PROXY_GRANTING_TICKET_TIMER")
    @Metered(name = "CREATE_PROXY_GRANTING_TICKET_METER")
    @Counted(name="CREATE_PROXY_GRANTING_TICKET_COUNTER", monotonic=true)
    @Override
    public ProxyGrantingTicket createProxyGrantingTicket(final String serviceTicketId, final AuthenticationContext context)
            throws AuthenticationException, AbstractTicketException {

        final ServiceTicket serviceTicket =  this.ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

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

        final Authentication authentication = context.getAuthentication();
        final ProxyGrantingTicketFactory factory = this.ticketFactory.get(ProxyGrantingTicket.class);
        final ProxyGrantingTicket proxyGrantingTicket = factory.create(serviceTicket, authentication);

        logger.debug("Generated proxy granting ticket [{}] based off of [{}]", proxyGrantingTicket, serviceTicketId);
        this.ticketRegistry.addTicket(proxyGrantingTicket);

        doPublishEvent(new CasProxyGrantingTicketCreatedEvent(this, proxyGrantingTicket));

        return proxyGrantingTicket;

    }

    @Audit(
        action="SERVICE_TICKET_VALIDATE",
        actionResolverName="VALIDATE_SERVICE_TICKET_RESOLVER",
        resourceResolverName="VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Timed(name="VALIDATE_SERVICE_TICKET_TIMER")
    @Metered(name="VALIDATE_SERVICE_TICKET_METER")
    @Counted(name="VALIDATE_SERVICE_TICKET_COUNTER", monotonic=true)
    @Override
    public Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws AbstractTicketException {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        verifyRegisteredServiceProperties(registeredService, service);

        final ServiceTicket serviceTicket =  this.ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

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

            final RegisteredServiceAttributeReleasePolicy attributePolicy = registeredService.getAttributeReleasePolicy();
            logger.debug("Attribute policy [{}] is associated with service [{}]", attributePolicy, registeredService);
            
            @SuppressWarnings("unchecked")
            final Map<String, Object> attributesToRelease = attributePolicy != null
                    ? attributePolicy.getAttributes(principal) : Collections.EMPTY_MAP;
            
            final String principalId = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service);
            final Principal modifiedPrincipal = this.principalFactory.createPrincipal(principalId, attributesToRelease);
            final AuthenticationBuilder builder = DefaultAuthenticationBuilder.newInstance(authentication);
            builder.setPrincipal(modifiedPrincipal);

            final Assertion assertion = new ImmutableAssertion(
                    builder.build(),
                    serviceTicket.getGrantingTicket().getChainedAuthentications(),
                    serviceTicket.getService(),
                    serviceTicket.isFromNewLogin());

            doPublishEvent(new CasServiceTicketValidatedEvent(this, serviceTicket, assertion));

            return assertion;

        } finally {
            if (serviceTicket.isExpired()) {
                this.ticketRegistry.deleteTicket(serviceTicketId);
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
    @Override
    public TicketGrantingTicket createTicketGrantingTicket(final AuthenticationContext context)
            throws AuthenticationException, AbstractTicketException {

        final Authentication authentication = context.getAuthentication();
        final TicketGrantingTicketFactory factory = this.ticketFactory.get(TicketGrantingTicket.class);
        final TicketGrantingTicket ticketGrantingTicket = factory.create(authentication);

        this.ticketRegistry.addTicket(ticketGrantingTicket);

        doPublishEvent(new CasTicketGrantingTicketCreatedEvent(this, ticketGrantingTicket));

        return ticketGrantingTicket;


    }



}
