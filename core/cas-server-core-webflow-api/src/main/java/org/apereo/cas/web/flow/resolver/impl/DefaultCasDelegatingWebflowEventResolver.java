package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
public class DefaultCasDelegatingWebflowEventResolver extends AbstractCasWebflowEventResolver implements CasDelegatingWebflowEventResolver {

    private final List<CasWebflowEventResolver> orderedResolvers = new ArrayList<>(0);
    private final CasWebflowEventResolver selectiveResolver;

    public DefaultCasDelegatingWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext,
                                                    final CasWebflowEventResolver selectiveResolver) {
        super(webflowEventResolutionConfigurationContext);
        this.selectiveResolver = selectiveResolver;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        try {
            val credential = getCredentialFromContext(context);
            val service = WebUtils.getService(context);

            if (credential != null) {
                val builder = getWebflowEventResolutionConfigurationContext().getAuthenticationSystemSupport()
                    .handleInitialAuthenticationTransaction(service, credential);
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
                WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
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
                LOGGER.warn("{}: {}", e.getClass(), e.getMessage());
                LOGGER.debug(e.getMessage(), e);
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
        val registeredService = getWebflowEventResolutionConfigurationContext().getServicesManager().findServiceBy(service);
        LOGGER.trace("Locating authentication event in the request context...");
        val authn = WebUtils.getAuthentication(context);
        if (authn == null) {
            throw new IllegalArgumentException("Unable to locate authentication object in the webflow context");
        }
        LOGGER.trace("Enforcing access strategy policies for registered service [{}] and principal [{}]",
            registeredService, authn.getPrincipal());
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
        val result = getWebflowEventResolutionConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
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
    protected Collection<Event> resolveCandidateAuthenticationEvents(final RequestContext context,
                                                                     final Service service,
                                                                     final RegisteredService registeredService) {
        return this.orderedResolvers
            .stream()
            .map(resolver -> {
                LOGGER.debug("Resolving candidate authentication event for service [{}] using [{}]", service, resolver.getName());
                return resolver.resolveSingle(context);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Event::getId))
            .collect(Collectors.toList());
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
            LOGGER.warn("{}: {}", e.getClass(), e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }

        if (e.getCause() instanceof AuthenticationException || e.getCause() instanceof AbstractTicketException) {
            val ex = e.getCause();
            LOGGER.warn("{}: {}", ex.getClass(), ex.getMessage());
            LOGGER.debug(ex.getMessage(), ex);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, ex);
        }
        return null;
    }
}
