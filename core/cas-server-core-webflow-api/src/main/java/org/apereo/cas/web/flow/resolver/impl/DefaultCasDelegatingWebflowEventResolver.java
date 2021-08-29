package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public DefaultCasDelegatingWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext configurationContext,
                                                    final CasWebflowEventResolver selectiveResolver) {
        super(configurationContext);
        this.selectiveResolver = selectiveResolver;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val credential = getCredentialFromContext(context);
        val service = WebUtils.getService(context);
        try {

            if (credential != null) {
                val builder = getConfigurationContext().getAuthenticationSystemSupport()
                    .handleInitialAuthenticationTransaction(service, credential);
                builder.getInitialAuthentication().ifPresent(authn -> {
                    WebUtils.putAuthenticationResultBuilder(builder, context);
                    WebUtils.putAuthentication(authn, context);
                });
            }

            val registeredService = determineRegisteredServiceForEvent(context, service);
            LOGGER.trace("Attempting to resolve candidate authentication events for service [{}]", service);
            val resolvedEvents = resolveCandidateAuthenticationEvents(context, service, registeredService);
            if (!resolvedEvents.isEmpty()) {
                LOGGER.trace("Authentication events resolved for [{}] are [{}]. Selecting final event...", service, resolvedEvents);
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
                val msg = "Unable to locate authentication object in the webflow context";
                throw new IllegalArgumentException(new AuthenticationException(msg));
            }
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception exception) {
            var event = returnAuthenticationExceptionEventIfNeeded(exception, credential, service);
            if (event == null) {
                FunctionUtils.doIf(LOGGER.isDebugEnabled(),
                    e -> LOGGER.debug(exception.getMessage(), exception),
                    e -> LoggingUtils.warn(LOGGER, exception.getMessage(), exception))
                    .accept(exception);
                event = newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, exception);
            }
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return CollectionUtils.wrapSet(event);
        }
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

    private RegisteredService determineRegisteredServiceForEvent(final RequestContext context, final Service service) {
        if (service == null) {
            return null;
        }
        LOGGER.trace("Locating authentication event in the request context...");
        val authn = WebUtils.getAuthentication(context);
        if (authn == null) {
            val msg = "Unable to locate authentication object in the webflow context";
            throw new IllegalArgumentException(new AuthenticationException(msg));
        }
        LOGGER.trace("Locating service [{}] in service registry to determine authentication policy", service);
        val registeredService = getConfigurationContext().getServicesManager().findServiceBy(service);
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
            .build();
        val result = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        result.throwExceptionIfNeeded();
        return registeredService;
    }

    private Event returnAuthenticationExceptionEventIfNeeded(final Exception exception,
                                                             final Credential credential,
                                                             final WebApplicationService service) {
        val result = (exception instanceof AuthenticationException || exception instanceof AbstractTicketException)
            ? Optional.of(exception)
            : (exception.getCause() instanceof AuthenticationException || exception.getCause() instanceof AbstractTicketException)
            ? Optional.of(exception.getCause())
            : Optional.empty();
        return result
            .map(Exception.class::cast)
            .map(ex -> {
                FunctionUtils.doIf(LOGGER.isDebugEnabled(),
                    e -> LOGGER.debug(ex.getMessage(), ex),
                    e -> LOGGER.warn(ex.getMessage()))
                    .accept(exception);
                val attributes = new LocalAttributeMap<Serializable>(CasWebflowConstants.TRANSITION_ID_ERROR, ex);
                attributes.put(Credential.class.getName(), credential);
                attributes.put(WebApplicationService.class.getName(), service);
                return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, attributes);
            })
            .orElse(null);
    }
}
