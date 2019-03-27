package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasDelegatingWebflowEventResolver},
 * which handles the initial authentication attempt and calls upon a number of
 * embedded resolvers to produce the next event in the authentication flow.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Setter
public class DefaultCasDelegatingWebflowEventResolver extends AbstractCasWebflowEventResolver implements CasDelegatingWebflowEventResolver {

    private final List<CasWebflowEventResolver> orderedResolvers = new ArrayList<>();
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private CasWebflowEventResolver selectiveResolver;

    public DefaultCasDelegatingWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                    final CentralAuthenticationService centralAuthenticationService,
                                                    final ServicesManager servicesManager,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final CookieGenerator warnCookieGenerator,
                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                    final ApplicationEventPublisher eventPublisher,
                                                    final ConfigurableApplicationContext applicationContext) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
            warnCookieGenerator, authenticationSelectionStrategies, eventPublisher, applicationContext);
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;

    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        try {
            val credential = getCredentialFromContext(context);
            val service = WebUtils.getService(context);
            if (credential != null) {
                val builder = this.authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credential);
                if (builder.getInitialAuthentication().isPresent()) {
                    WebUtils.putAuthenticationResultBuilder(builder, context);
                    WebUtils.putAuthentication(builder.getInitialAuthentication().get(), context);
                }
            }
            val registeredService = determineRegisteredServiceForEvent(context, service);
            LOGGER.trace("Attempting to resolve candidate authentication events for service [{}]", service);
            val resolvedEvents = resolveCandidateAuthenticationEvents(context, service, registeredService);
            if (!resolvedEvents.isEmpty()) {
                LOGGER.trace("The set of authentication events resolved for [{}] are [{}]. Beginning to select the final event...", service, resolvedEvents);
                putResolvedEventsAsAttribute(context, resolvedEvents);
                val finalResolvedEvent = this.selectiveResolver.resolveSingle(context);
                LOGGER.debug("The final authentication event resolved for [{}] is [{}]", service, finalResolvedEvent);
                if (finalResolvedEvent != null) {
                    return CollectionUtils.wrapSet(finalResolvedEvent);
                }
            } else {
                LOGGER.trace("No candidate authentication events were resolved for service [{}]", service);
            }

            val builder = WebUtils.getAuthenticationResultBuilder(context);
            if (builder == null) {
                throw new IllegalArgumentException("No authentication result builder can be located in the context");
            }
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            var event = returnAuthenticationExceptionEventIfNeeded(e);
            if (event == null) {
                LOGGER.warn(e.getMessage(), e);
                event = newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, e);
            }
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return CollectionUtils.wrapSet(event);
        }
    }

    private RegisteredService determineRegisteredServiceForEvent(final RequestContext context, final Service service) {
        if (service == null) {
            return null;
        }

        LOGGER.trace("Locating service [{}] in service registry to determine authentication policy", service);
        val registeredService = this.servicesManager.findServiceBy(service);
        LOGGER.trace("Locating authentication event in the request context...");
        val authn = WebUtils.getAuthentication(context);
        if (authn == null) {
            throw new IllegalArgumentException("Unable to locate authentication object in the webflow context");
        }
        LOGGER.trace("Enforcing access strategy policies for registered service [{}] and principal [{}]", registeredService, authn.getPrincipal());
        val unauthorizedRedirectUrl = registeredService.getAccessStrategy().getUnauthorizedRedirectUrl();
        if (unauthorizedRedirectUrl != null) {
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, unauthorizedRedirectUrl);
        }

        val audit = AuditableContext.builder()
            .service(service)
            .authentication(authn)
            .registeredService(registeredService)
            .retrievePrincipalAttributesFromReleasePolicy(Boolean.FALSE)
            .build();
        val result = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        result.throwExceptionIfNeeded();
        return registeredService;
    }

    /**
     * Resolve candidate authentication events set.
     *
     * @param context           the context
     * @param service           the service
     * @param registeredService the registered service
     * @return the set
     */
    protected Set<Event> resolveCandidateAuthenticationEvents(final RequestContext context,
                                                              final Service service,
                                                              final RegisteredService registeredService) {

        val byEventId = Comparator.comparing(Event::getId);
        val supplier = (Supplier<TreeSet<Event>>) () -> new TreeSet<>(byEventId);

        return this.orderedResolvers
            .stream()
            .map(resolver -> {
                LOGGER.debug("Resolving candidate authentication event for service [{}] using [{}]", service, resolver.getName());
                return resolver.resolveSingle(context);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(supplier));
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver r) {
        if (r != null) {
            orderedResolvers.add(r);
        }
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver r, final int index) {
        if (r != null) {
            orderedResolvers.add(index, r);
        }
    }

    private Event returnAuthenticationExceptionEventIfNeeded(final Exception e) {
        if (e instanceof AuthenticationException || e instanceof AbstractTicketException) {
            LOGGER.debug(e.getMessage(), e);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }

        if (e.getCause() instanceof AuthenticationException || e.getCause() instanceof AbstractTicketException) {
            val ex = e.getCause();
            LOGGER.debug(ex.getMessage(), ex);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, ex);
        }
        return null;
    }
}
