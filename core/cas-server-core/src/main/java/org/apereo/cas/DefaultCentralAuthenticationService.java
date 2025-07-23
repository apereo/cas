package org.apereo.cas;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.multitenancy.UnknownTenantException;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.support.events.ticket.CasProxyGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasProxyTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketValidatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationException;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete implementation of a {@link CentralAuthenticationService}, and also the
 * central, organizing component of CAS' internal implementation.
 * This class is thread-safe.
 *
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @author Dmitry Kopylenko
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Slf4j
public class DefaultCentralAuthenticationService extends AbstractCentralAuthenticationService {
    @Serial
    private static final long serialVersionUID = -8943828074939533986L;

    public DefaultCentralAuthenticationService(final CentralAuthenticationServiceContext context) {
        super(context);
    }


    @Audit(
        action = AuditableActions.TICKET_GRANTING_TICKET,
        actionResolverName = AuditActionResolvers.CREATE_TICKET_GRANTING_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER)
    @Override
    public Ticket createTicketGrantingTicket(final AuthenticationResult authenticationResult) throws Throwable {
        val authentication = authenticationResult.getAuthentication();
        var service = authenticationResult.getService();
        val clientInfo = ClientInfoHolder.getClientInfo();

        if (service != null) {
            service = resolveServiceFromAuthenticationRequest(service);
            LOGGER.debug("Resolved service [{}] from the authentication request", service);
            val registeredService = configurationContext.getServicesManager().findServiceBy(service);
            enforceRegisteredServiceAccess(authentication, service, registeredService);
        }

        val factory = (TicketGrantingTicketFactory) configurationContext.getTicketFactory().get(TicketGrantingTicket.class);
        val ticketGrantingTicket = factory.create(authentication, service);
        val addedTicket = configurationContext.getTicketRegistry().addTicket(ticketGrantingTicket);
        doPublishEvent(new CasTicketGrantingTicketCreatedEvent(this, ticketGrantingTicket, clientInfo));
        return addedTicket;
    }

    @Audit(
        action = AuditableActions.SERVICE_TICKET,
        actionResolverName = AuditActionResolvers.GRANT_SERVICE_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.GRANT_SERVICE_TICKET_RESOURCE_RESOLVER)
    @Override
    public Ticket grantServiceTicket(final String ticketGrantingTicketId, final Service service,
                                     final AuthenticationResult authenticationResult) throws Throwable {

        val credentialProvided = authenticationResult != null && authenticationResult.isCredentialProvided();
        val clientInfo = ClientInfoHolder.getClientInfo();
        return configurationContext.getLockRepository().execute(ticketGrantingTicketId,
            Unchecked.supplier(new CheckedSupplier<Ticket>() {
                @Override
                public Ticket get() throws Throwable {
                    val ticketGrantingTicket = configurationContext.getTicketRegistry().getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
                    val selectedService = resolveServiceFromAuthenticationRequest(service);
                    val registeredService = configurationContext.getServicesManager().findServiceBy(selectedService);

                    val currentAuthentication = evaluatePossibilityOfMixedPrincipals(authenticationResult, ticketGrantingTicket);
                    RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(registeredService,
                        selectedService, ticketGrantingTicket, credentialProvided);
                    evaluateProxiedServiceIfNeeded(selectedService, ticketGrantingTicket, registeredService);
                    getAuthenticationSatisfiedByPolicy(currentAuthentication, selectedService, registeredService);

                    val latestAuthentication = ticketGrantingTicket.getRoot().getAuthentication();
                    val principal = latestAuthentication.getPrincipal();
                    val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                        .registeredService(registeredService)
                        .service(service)
                        .principal(principal)
                        .applicationContext(configurationContext.getApplicationContext())
                        .build();
                    val merger = CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
                    val policyAttributes = registeredService.getAttributeReleasePolicy().getAttributes(releasePolicyContext);
                    var accessAttributes = CoreAuthenticationUtils.mergeAttributes(principal.getAttributes(), latestAuthentication.getAttributes(), merger);
                    accessAttributes = CoreAuthenticationUtils.mergeAttributes(accessAttributes, policyAttributes, merger);
                    val accessPrincipal = configurationContext.getPrincipalFactory().createPrincipal(principal.getId(), accessAttributes);
                    enforceRegisteredServiceAccess(selectedService, registeredService, accessPrincipal);

                    val factory = (ServiceTicketFactory) configurationContext.getTicketFactory().get(ServiceTicket.class);
                    val serviceTicket = factory.create(ticketGrantingTicket, selectedService, credentialProvided, ServiceTicket.class);
                    if (!ticketGrantingTicket.isStateless()) {
                        configurationContext.getTicketRegistry().updateTicket(ticketGrantingTicket);
                    }
                    val addedServiceTicket = configurationContext.getTicketRegistry().addTicket(serviceTicket);
                    LOGGER.info("Granted service ticket [{}] for service [{}] and principal [{}]",
                        serviceTicket.getId(), DigestUtils.abbreviate(selectedService.getId()), principal.getId());
                    doPublishEvent(new CasServiceTicketGrantedEvent(this, ticketGrantingTicket, serviceTicket, clientInfo));
                    return addedServiceTicket;
                }
            })).orElseThrow(() -> new InvalidTicketException(ticketGrantingTicketId));
    }

    @Audit(
        action = AuditableActions.PROXY_TICKET,
        actionResolverName = AuditActionResolvers.GRANT_PROXY_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.GRANT_PROXY_TICKET_RESOURCE_RESOLVER)
    @Override
    public Ticket grantProxyTicket(final String proxyGrantingTicketId, final Service service) throws AbstractTicketException {
        return configurationContext.getLockRepository().execute(proxyGrantingTicketId,
                () -> FunctionUtils.doUnchecked(() -> {
                    val proxyGrantingTicket = configurationContext.getTicketRegistry().getTicket(proxyGrantingTicketId, ProxyGrantingTicket.class);
                    val registeredService = configurationContext.getServicesManager().findServiceBy(service);
                    try {
                        enforceRegisteredServiceAccess(service, proxyGrantingTicket, registeredService);
                        RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(registeredService, service, proxyGrantingTicket);
                    } catch (final Throwable e) {
                        LoggingUtils.warn(LOGGER, e);
                        throw new UnauthorizedSsoServiceException();
                    }

                    evaluateProxiedServiceIfNeeded(service, proxyGrantingTicket, registeredService);
                    getAuthenticationSatisfiedByPolicy(proxyGrantingTicket.getRoot().getAuthentication(), service, registeredService);

                    val authentication = proxyGrantingTicket.getRoot().getAuthentication();
                    val principal = authentication.getPrincipal();
                    val factory = (ProxyTicketFactory) configurationContext.getTicketFactory().get(ProxyTicket.class);
                    val proxyTicket = factory.create(proxyGrantingTicket, service);
                    val clientInfo = ClientInfoHolder.getClientInfo();
                    if (!proxyGrantingTicket.isStateless()) {
                        configurationContext.getTicketRegistry().updateTicket(proxyGrantingTicket);
                    }
                    val addedProxyTicket = configurationContext.getTicketRegistry().addTicket(proxyTicket);
                    LOGGER.info("Granted proxy ticket [{}] for service [{}] for user [{}]",
                        addedProxyTicket.getId(), service.getId(), principal.getId());
                    doPublishEvent(new CasProxyTicketGrantedEvent(this, proxyGrantingTicket, addedProxyTicket, clientInfo));
                    return addedProxyTicket;
                }))
            .orElseThrow(UnauthorizedProxyingException::new);
    }

    @Audit(
        action = AuditableActions.SERVICE_TICKET_VALIDATE,
        actionResolverName = AuditActionResolvers.VALIDATE_SERVICE_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER)
    @Override
    public Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws Throwable {
        if (!isTicketAuthenticityVerified(serviceTicketId)) {
            LOGGER.info("Service ticket [{}] is not a valid ticket issued by CAS.", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }
        val serviceTicket = configurationContext.getTicketRegistry().getTicket(serviceTicketId, ServiceTicket.class);
        if (serviceTicket == null) {
            LOGGER.warn("Service ticket [{}] does not exist.", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }
        if (!(serviceTicket.getTicketGrantingTicket() instanceof TicketGrantingTicket) && !serviceTicket.isStateless()) {
            LOGGER.warn("Service ticket [{}] is not assigned a valid ticket granting ticket", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }

        try {
            val selectedService = resolveServiceFromAuthenticationRequest(serviceTicket.getService());
            val resolvedService = resolveServiceFromAuthenticationRequest(service);
            LOGGER.debug("Resolved service [{}] from the authentication request with service [{}] linked to service ticket [{}]",
                resolvedService, selectedService, serviceTicket.getId());

            configurationContext.getLockRepository().execute(serviceTicket.getId(),
                Unchecked.supplier(() -> {
                    if (serviceTicket.isExpired()) {
                        LOGGER.info("Service ticket [{}] has expired.", serviceTicketId);
                        throw new InvalidTicketException(serviceTicketId);
                    }
                    if (!configurationContext.getServiceMatchingStrategy().matches(selectedService, resolvedService)) {
                        LOGGER.error("Service ticket [{}] with service [{}] does not match supplied service [{}]",
                            serviceTicketId, serviceTicket.getService().getId(), resolvedService.getId());
                        throw new UnrecognizableServiceForServiceTicketValidationException(selectedService);
                    }
                    if (StringUtils.isNotBlank(serviceTicket.getTenantId())) {
                        if (!Strings.CI.equals(resolvedService.getTenant(), serviceTicket.getTenantId())) {
                            LOGGER.warn("Service ticket [{}] is not assigned to the same tenant [{}] as the service [{}]",
                                serviceTicketId, serviceTicket.getTenantId(), resolvedService.getId());
                            throw new UnknownTenantException("Unknown tenant %s for service ticket %s"
                                .formatted(resolvedService.getTenant(), serviceTicketId));
                        }
                        if (configurationContext.getTenantExtractor().getTenantsManager().findTenant(serviceTicket.getTenantId()).isEmpty()) {
                            LOGGER.warn("Service ticket [{}] is not assigned to a known valid tenant [{}] for service [{}]",
                                serviceTicketId, serviceTicket.getTenantId(), resolvedService.getId());
                            throw new UnknownTenantException("Unknown tenant %s for service ticket %s"
                                .formatted(serviceTicket.getTenantId(), serviceTicketId));
                        }
                    }
                    
                    serviceTicket.update();
                    if (!serviceTicket.isStateless()) {
                        configurationContext.getTicketRegistry().updateTicket(serviceTicket);
                    }
                    return serviceTicket;
                }));

            val registeredService = configurationContext.getServicesManager().findServiceBy(selectedService);
            LOGGER.trace("Located registered service definition [{}] from [{}] to handle validation request", registeredService, selectedService);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(selectedService, registeredService);

            val ticketGrantingTicket = (TicketGrantingTicket) serviceTicket.getTicketGrantingTicket();
            var authentication = serviceTicket.isStateless()
                ? serviceTicket.getAuthentication()
                : ticketGrantingTicket.getRoot().getAuthentication();

            authentication = getAuthenticationSatisfiedByPolicy(authentication, selectedService, registeredService);
            val principal = serviceTicket.isStateless() ? rebuildStatelessTicketPrincipal(serviceTicket) : authentication.getPrincipal();
            val attributePolicy = Objects.requireNonNull(registeredService.getAttributeReleasePolicy());
            LOGGER.debug("Attribute policy [{}] is associated with service [{}]", attributePolicy, registeredService);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(selectedService)
                .principal(principal)
                .applicationContext(configurationContext.getApplicationContext())
                .build();
            val attributesToRelease = attributePolicy.getAttributes(context);
            LOGGER.debug("Calculated attributes for release per the release policy are [{}]", attributesToRelease.keySet());

            val builder = DefaultAuthenticationBuilder.of(
                configurationContext.getApplicationContext(),
                principal,
                configurationContext.getPrincipalFactory(),
                attributesToRelease,
                selectedService,
                registeredService,
                authentication);
            LOGGER.debug("Principal determined for release to [{}] is [{}]",
                registeredService.getServiceId(), builder.getPrincipal().getId());

            builder.addAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
                CollectionUtils.wrap(((RenewableServiceTicket) serviceTicket).isFromNewLogin()));
            builder.addAttribute(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
                CollectionUtils.wrap(CoreAuthenticationUtils.isRememberMeAuthentication(authentication)));

            val finalAuthentication = builder.build();
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(service)
                .applicationContext(configurationContext.getApplicationContext())
                .principal(principal)
                .build();
            val policyAttributes = registeredService.getAttributeReleasePolicy().getAttributes(releasePolicyContext);
            val merger = CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
            var accessAttributes = CoreAuthenticationUtils.mergeAttributes(principal.getAttributes(), authentication.getAttributes(), merger);
            accessAttributes = CoreAuthenticationUtils.mergeAttributes(accessAttributes, finalAuthentication.getPrincipal().getAttributes(), merger);
            accessAttributes = CoreAuthenticationUtils.mergeAttributes(accessAttributes, finalAuthentication.getAttributes(), merger);
            accessAttributes = CoreAuthenticationUtils.mergeAttributes(accessAttributes, policyAttributes, merger);
            val accessPrincipal = configurationContext.getPrincipalFactory().createPrincipal(principal.getId(), accessAttributes);

            enforceRegisteredServiceAccess(selectedService, registeredService, accessPrincipal);

            val assertionContext = serviceTicket.isStateless()
                ? CollectionUtils.<String, Serializable>wrap(Principal.class.getName(), authentication.getPrincipal().getId())
                : CollectionUtils.<String, Serializable>wrap(TicketGrantingTicket.class.getName(), ticketGrantingTicket.getRoot().getId());

            val assertion = DefaultAssertionBuilder.builder()
                .primaryAuthentication(finalAuthentication)
                .originalAuthentication(authentication)
                .service(selectedService)
                .registeredService(registeredService)
                .authentications(serviceTicket.isStateless()
                    ? List.of(serviceTicket.getAuthentication())
                    : ticketGrantingTicket.getChainedAuthentications())
                .newLogin(((RenewableServiceTicket) serviceTicket).isFromNewLogin())
                .stateless(serviceTicket.isStateless())
                .context(assertionContext)
                .build()
                .assemble();
            val clientInfo = ClientInfoHolder.getClientInfo();
            doPublishEvent(new CasServiceTicketValidatedEvent(this, serviceTicket, assertion, clientInfo));
            return assertion;
        } finally {
            if (!serviceTicket.isStateless()) {
                if (serviceTicket.isExpired()) {
                    configurationContext.getTicketRegistry().deleteTicket(serviceTicketId);
                } else {
                    configurationContext.getTicketRegistry().updateTicket(serviceTicket);
                }
            }
        }
    }


    @Audit(
        action = AuditableActions.PROXY_GRANTING_TICKET,
        actionResolverName = AuditActionResolvers.CREATE_PROXY_GRANTING_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER)
    @Override
    public Ticket createProxyGrantingTicket(final String serviceTicketId,
                                            final AuthenticationResult authenticationResult) throws Throwable {

        val serviceTicket = configurationContext.getTicketRegistry().getTicket(serviceTicketId, ServiceTicket.class);
        if (serviceTicket == null || serviceTicket.isExpired()) {
            LOGGER.debug("ServiceTicket [{}] has expired or cannot be found in the ticket registry", serviceTicketId);
            throw new InvalidTicketException(serviceTicketId);
        }
        val registeredService = (CasModelRegisteredService) configurationContext.getServicesManager()
            .findServiceBy(serviceTicket.getService());

        val ctx = AuditableContext.builder()
            .serviceTicket(serviceTicket)
            .authenticationResult(authenticationResult)
            .registeredService(registeredService)
            .build();

        enforceRegisteredServiceAccess(ctx);

        if (!registeredService.getProxyPolicy().isAllowedToProxy()) {
            LOGGER.warn("Service [{}] attempted to proxy, but is not allowed.", serviceTicket.getService().getId());
            throw new UnauthorizedProxyingException();
        }

        return configurationContext.getLockRepository().execute(serviceTicket.getId(),
                Unchecked.supplier(() -> {
                    val authentication = authenticationResult.getAuthentication();
                    val factory = (ProxyGrantingTicketFactory) configurationContext.getTicketFactory().get(ProxyGrantingTicket.class);
                    val proxyGrantingTicket = factory.create(serviceTicket, authentication);
                    val addedTicket = configurationContext.getTicketRegistry().addTicket(proxyGrantingTicket);
                    LOGGER.debug("Generated proxy granting ticket [{}] based off of [{}]", proxyGrantingTicket, serviceTicketId);
                    if (!serviceTicket.isStateless()) {
                        configurationContext.getTicketRegistry().updateTicket(serviceTicket.getTicketGrantingTicket());
                    }
                    val clientInfo = ClientInfoHolder.getClientInfo();
                    doPublishEvent(new CasProxyGrantingTicketCreatedEvent(this, addedTicket, clientInfo));
                    return addedTicket;
                }))
            .orElseThrow(UnauthorizedProxyingException::new);
    }

    private void enforceRegisteredServiceAccess(final Authentication authentication, final Service service,
                                                final RegisteredService registeredService) throws Throwable {

        val attributeReleaseContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(authentication.getPrincipal())
            .applicationContext(configurationContext.getApplicationContext())
            .build();
        val releasingAttributes = registeredService.getAttributeReleasePolicy().getAttributes(attributeReleaseContext);
        releasingAttributes.putAll(authentication.getAttributes());

        val accessStrategyAttributes = CoreAuthenticationUtils.mergeAttributes(
            authentication.getPrincipal().getAttributes(), releasingAttributes);
        val accessStrategyPrincipal = configurationContext.getPrincipalFactory()
            .createPrincipal(authentication.getPrincipal().getId(), accessStrategyAttributes);
        val audit = AuditableContext.builder()
            .service(service)
            .principal(accessStrategyPrincipal)
            .registeredService(registeredService)
            .build();
        enforceRegisteredServiceAccess(audit);
    }

    protected void enforceRegisteredServiceAccess(final AuditableContext audit) throws Throwable {
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();
    }

    private void enforceRegisteredServiceAccess(final Service service, final RegisteredService registeredService,
                                                final Principal principal) throws Throwable {
        val audit = AuditableContext.builder()
            .service(service)
            .principal(principal)
            .registeredService(registeredService)
            .build();
        enforceRegisteredServiceAccess(audit);
    }

    private void enforceRegisteredServiceAccess(final Service service, final TicketGrantingTicket ticket,
                                                final RegisteredService registeredService) throws Throwable {
        val audit = AuditableContext.builder()
            .service(service)
            .ticketGrantingTicket(ticket)
            .registeredService(registeredService)
            .build();
        enforceRegisteredServiceAccess(audit);
    }

    protected Principal rebuildStatelessTicketPrincipal(final ServiceTicket serviceTicket) throws Throwable {
        val authentication = serviceTicket.getAuthentication();
        return configurationContext.getPrincipalResolver()
            .resolve(new BasicIdentifiableCredential(authentication.getPrincipal().getId()),
                Optional.of(authentication.getPrincipal()), Optional.empty(),
                Optional.of(serviceTicket.getService()));
    }

    private static Authentication evaluatePossibilityOfMixedPrincipals(final AuthenticationResult context,
                                                                       final TicketGrantingTicket ticketGrantingTicket) {
        if (context == null) {
            LOGGER.warn("Provided authentication result is undefined to evaluate for mixed principals");
            return null;
        }
        val currentAuthentication = context.getAuthentication();
        if (currentAuthentication != null) {
            val original = ticketGrantingTicket.getAuthentication();
            if (!currentAuthentication.getPrincipal().equals(original.getPrincipal())) {
                throw new MixedPrincipalException(currentAuthentication,
                    currentAuthentication.getPrincipal(), original.getPrincipal());
            }
        }
        return currentAuthentication;
    }
}
