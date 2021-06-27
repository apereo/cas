package org.apereo.cas;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.support.events.ticket.CasProxyGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasProxyTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketValidatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationException;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.DefaultAssertionBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * Concrete implementation of a {@link CentralAuthenticationService}, and also the
 * central, organizing component of CAS' internal implementation.
 * This class is threadsafe.
 *
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @author Dmitry Kopylenko
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
public class DefaultCentralAuthenticationService extends AbstractCentralAuthenticationService {
    private static final long serialVersionUID = -8943828074939533986L;

    private final transient Object serviceTicketValidationLock = new Object();

    public DefaultCentralAuthenticationService(final ApplicationEventPublisher applicationEventPublisher,
                                               final TicketRegistry ticketRegistry,
                                               final ServicesManager servicesManager,
                                               final TicketFactory ticketFactory,
                                               final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                               final ContextualAuthenticationPolicyFactory<ServiceContext> serviceContextAuthenticationPolicyFactory,
                                               final PrincipalFactory principalFactory,
                                               final CipherExecutor<String, String> cipherExecutor,
                                               final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                               final ServiceMatchingStrategy serviceMatchingStrategy) {
        super(applicationEventPublisher, ticketRegistry, servicesManager, ticketFactory,
            authenticationRequestServiceSelectionStrategies, serviceContextAuthenticationPolicyFactory,
            principalFactory, cipherExecutor, registeredServiceAccessStrategyEnforcer,
            serviceMatchingStrategy);
    }

    @Audit(
        action = AuditableActions.SERVICE_TICKET,
        actionResolverName = AuditActionResolvers.GRANT_SERVICE_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.GRANT_SERVICE_TICKET_RESOURCE_RESOLVER)
    @Override
    public ServiceTicket grantServiceTicket(final String ticketGrantingTicketId, final Service service, final AuthenticationResult authenticationResult)
        throws AuthenticationException, AbstractTicketException {

        val credentialProvided = authenticationResult != null && authenticationResult.isCredentialProvided();
        val ticketGrantingTicket = getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        val selectedService = resolveServiceFromAuthenticationRequest(service);
        val registeredService = this.servicesManager.findServiceBy(selectedService);

        enforceRegisteredServiceAccess(selectedService, ticketGrantingTicket, registeredService);

        val currentAuthentication = evaluatePossibilityOfMixedPrincipals(authenticationResult, ticketGrantingTicket);
        RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(registeredService, selectedService, ticketGrantingTicket, credentialProvided);
        evaluateProxiedServiceIfNeeded(selectedService, ticketGrantingTicket, registeredService);

        getAuthenticationSatisfiedByPolicy(currentAuthentication, new ServiceContext(selectedService, registeredService));

        val latestAuthentication = ticketGrantingTicket.getRoot().getAuthentication();
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(latestAuthentication);
        val principal = latestAuthentication.getPrincipal();
        val factory = (ServiceTicketFactory) this.ticketFactory.get(ServiceTicket.class);
        val serviceTicket = factory.create(ticketGrantingTicket, selectedService, credentialProvided, ServiceTicket.class);
        this.ticketRegistry.updateTicket(ticketGrantingTicket);
        this.ticketRegistry.addTicket(serviceTicket);

        LOGGER.info("Granted service ticket [{}] for service [{}] and principal [{}]",
            serviceTicket.getId(), DigestUtils.abbreviate(selectedService.getId()), principal.getId());
        doPublishEvent(new CasServiceTicketGrantedEvent(this, ticketGrantingTicket, serviceTicket));
        return serviceTicket;
    }

    @Audit(
        action = AuditableActions.PROXY_TICKET,
        actionResolverName = AuditActionResolvers.GRANT_PROXY_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.GRANT_PROXY_TICKET_RESOURCE_RESOLVER)
    @Override
    public ProxyTicket grantProxyTicket(final String proxyGrantingTicket, final Service service)
        throws AbstractTicketException {

        val proxyGrantingTicketObject = getTicket(proxyGrantingTicket, ProxyGrantingTicket.class);
        val registeredService = servicesManager.findServiceBy(service);

        try {
            enforceRegisteredServiceAccess(service, proxyGrantingTicketObject, registeredService);
            RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(registeredService, service, proxyGrantingTicketObject);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            throw new UnauthorizedSsoServiceException();
        }

        evaluateProxiedServiceIfNeeded(service, proxyGrantingTicketObject, registeredService);

        getAuthenticationSatisfiedByPolicy(proxyGrantingTicketObject.getRoot().getAuthentication(), new ServiceContext(service, registeredService));

        val authentication = proxyGrantingTicketObject.getRoot().getAuthentication();
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);

        val principal = authentication.getPrincipal();
        val factory = (ProxyTicketFactory) ticketFactory.get(ProxyTicket.class);
        val proxyTicket = factory.create(proxyGrantingTicketObject, service, ProxyTicket.class);

        this.ticketRegistry.updateTicket(proxyGrantingTicketObject);
        this.ticketRegistry.addTicket(proxyTicket);

        LOGGER.info("Granted proxy ticket [{}] for service [{}] for user [{}]",
            proxyTicket.getId(), service.getId(), principal.getId());

        doPublishEvent(new CasProxyTicketGrantedEvent(this, proxyGrantingTicketObject, proxyTicket));
        return proxyTicket;
    }


    @Audit(
        action = AuditableActions.PROXY_GRANTING_TICKET,
        actionResolverName = AuditActionResolvers.CREATE_PROXY_GRANTING_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER)
    @Override
    public ProxyGrantingTicket createProxyGrantingTicket(final String serviceTicketId, final AuthenticationResult authenticationResult)
        throws AuthenticationException, AbstractTicketException {

        AuthenticationCredentialsThreadLocalBinder.bindCurrent(authenticationResult.getAuthentication());
        val serviceTicket = this.ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

        if (serviceTicket == null || serviceTicket.isExpired()) {
            LOGGER.debug("ServiceTicket [{}] has expired or cannot be found in the ticket registry", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }
        val registeredService = this.servicesManager.findServiceBy(serviceTicket.getService());

        val ctx = AuditableContext.builder()
            .serviceTicket(serviceTicket)
            .authenticationResult(authenticationResult)
            .registeredService(registeredService)
            .build();

        val result = this.registeredServiceAccessStrategyEnforcer.execute(ctx);
        result.throwExceptionIfNeeded();

        if (!registeredService.getProxyPolicy().isAllowedToProxy()) {
            LOGGER.warn("Service [{}] attempted to proxy, but is not allowed.", serviceTicket.getService().getId());
            throw new UnauthorizedProxyingException();
        }

        val authentication = authenticationResult.getAuthentication();
        val factory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);
        val proxyGrantingTicket = factory.create(serviceTicket, authentication, ProxyGrantingTicket.class);

        LOGGER.debug("Generated proxy granting ticket [{}] based off of [{}]", proxyGrantingTicket, serviceTicketId);
        this.ticketRegistry.addTicket(proxyGrantingTicket);

        doPublishEvent(new CasProxyGrantingTicketCreatedEvent(this, proxyGrantingTicket));

        return proxyGrantingTicket;
    }

    @Audit(
        action = AuditableActions.SERVICE_TICKET_VALIDATE,
        actionResolverName = AuditActionResolvers.VALIDATE_SERVICE_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER)
    @Override
    public Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws AbstractTicketException {

        if (!isTicketAuthenticityVerified(serviceTicketId)) {
            LOGGER.info("Service ticket [{}] is not a valid ticket issued by CAS.", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }

        val serviceTicket = ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class);

        if (serviceTicket == null) {
            LOGGER.warn("Service ticket [{}] does not exist.", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }

        try {
            val selectedService = resolveServiceFromAuthenticationRequest(serviceTicket.getService());
            val resolvedService = resolveServiceFromAuthenticationRequest(service);
            LOGGER.debug("Resolved service [{}] from the authentication request with service [{}] linked to service ticket [{}]",
                resolvedService, selectedService, serviceTicket.getId());
            /*
             * Synchronization on ticket object in case of cache based registry doesn't serialize
             * access to critical section. The reason is that cache pulls serialized data and
             * builds new object, most likely for each pull. Is this synchronization needed here?
             */
            synchronized (this.serviceTicketValidationLock) {
                if (serviceTicket.isExpired()) {
                    LOGGER.info("ServiceTicket [{}] has expired.", serviceTicketId);
                    throw new InvalidTicketException(serviceTicketId);
                }

                if (!this.serviceMatchingStrategy.matches(selectedService, resolvedService)) {
                    LOGGER.error("Service ticket [{}] with service [{}] does not match supplied service [{}]",
                        serviceTicketId, serviceTicket.getService().getId(), resolvedService.getId());
                    throw new UnrecognizableServiceForServiceTicketValidationException(selectedService);
                }
                val ticketState = TicketState.class.cast(serviceTicket);
                ticketState.update();
            }


            val registeredService = this.servicesManager.findServiceBy(selectedService);
            LOGGER.trace("Located registered service definition [{}] from [{}] to handle validation request", registeredService, selectedService);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(selectedService, registeredService);

            val root = serviceTicket.getTicketGrantingTicket().getRoot();
            val authentication = getAuthenticationSatisfiedByPolicy(root.getAuthentication(),
                new ServiceContext(selectedService, registeredService));
            val principal = authentication.getPrincipal();

            val attributePolicy = registeredService.getAttributeReleasePolicy();
            LOGGER.debug("Attribute policy [{}] is associated with service [{}]", attributePolicy, registeredService);

            val attributesToRelease = attributePolicy != null
                ? attributePolicy.getAttributes(principal, selectedService, registeredService)
                : new HashMap<String, List<Object>>();

            LOGGER.debug("Calculated attributes for release per the release policy are [{}]",
                attributesToRelease.keySet());

            val principalId = registeredService.getUsernameAttributeProvider()
                .resolveUsername(principal, selectedService, registeredService);
            val builder = DefaultAuthenticationBuilder.of(
                principal,
                this.principalFactory,
                attributesToRelease,
                selectedService,
                registeredService,
                authentication);
            LOGGER.debug("Principal determined for release to [{}] is [{}]", registeredService.getServiceId(), principalId);
            
            builder.addAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
                CollectionUtils.wrap(serviceTicket.isFromNewLogin()));
            builder.addAttribute(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                CollectionUtils.wrap(CoreAuthenticationUtils.isRememberMeAuthentication(authentication)));
            
            val finalAuthentication = builder.build();

            enforceRegisteredServiceAccess(finalAuthentication, selectedService, registeredService);

            AuthenticationCredentialsThreadLocalBinder.bindCurrent(finalAuthentication);

            val assertion = new DefaultAssertionBuilder(finalAuthentication)
                .with(selectedService)
                .with(serviceTicket.getTicketGrantingTicket().getChainedAuthentications())
                .with(serviceTicket.isFromNewLogin())
                .build();
            
            doPublishEvent(new CasServiceTicketValidatedEvent(this, serviceTicket, assertion));
            return assertion;
        } finally {
            if (serviceTicket.isExpired()) {
                deleteTicket(serviceTicketId);
            } else {
                this.ticketRegistry.updateTicket(serviceTicket);
            }
        }
    }

    @Audit(
        action = AuditableActions.TICKET_GRANTING_TICKET,
        actionResolverName = AuditActionResolvers.CREATE_TICKET_GRANTING_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER)
    @Override
    public TicketGrantingTicket createTicketGrantingTicket(final AuthenticationResult authenticationResult)
        throws AuthenticationException, AbstractTicketException {

        val authentication = authenticationResult.getAuthentication();
        var service = authenticationResult.getService();
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);

        if (service != null) {
            service = resolveServiceFromAuthenticationRequest(service);
            LOGGER.debug("Resolved service [{}] from the authentication request", service);
            val registeredService = this.servicesManager.findServiceBy(service);
            enforceRegisteredServiceAccess(authentication, service, registeredService);
        }

        val factory = (TicketGrantingTicketFactory) this.ticketFactory.get(TicketGrantingTicket.class);
        val ticketGrantingTicket = factory.create(authentication, service, TicketGrantingTicket.class);

        this.ticketRegistry.addTicket(ticketGrantingTicket);
        doPublishEvent(new CasTicketGrantingTicketCreatedEvent(this, ticketGrantingTicket));
        return ticketGrantingTicket;
    }

    private void enforceRegisteredServiceAccess(final Authentication authentication, final Service service,
                                                final RegisteredService registeredService) {
        val audit = AuditableContext.builder()
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .build();
        val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        accessResult.throwExceptionIfNeeded();
    }

    private void enforceRegisteredServiceAccess(final Service service, final TicketGrantingTicket ticket, final RegisteredService registeredService) {
        val audit = AuditableContext.builder()
            .service(service)
            .ticketGrantingTicket(ticket)
            .registeredService(registeredService)
            .build();
        val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        accessResult.throwExceptionIfNeeded();
    }

    private static Authentication evaluatePossibilityOfMixedPrincipals(final AuthenticationResult context, final TicketGrantingTicket ticketGrantingTicket) {
        if (context == null) {
            return null;
        }
        val currentAuthentication = context.getAuthentication();
        if (currentAuthentication != null) {
            val original = ticketGrantingTicket.getAuthentication();
            if (!currentAuthentication.getPrincipal().equals(original.getPrincipal())) {
                throw new MixedPrincipalException(currentAuthentication, currentAuthentication.getPrincipal(), original.getPrincipal());
            }
        }
        return currentAuthentication;
    }
}
