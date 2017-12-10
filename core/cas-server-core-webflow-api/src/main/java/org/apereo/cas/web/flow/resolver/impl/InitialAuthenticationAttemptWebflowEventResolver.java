package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link InitialAuthenticationAttemptWebflowEventResolver},
 * which handles the initial authentication attempt and calls upon a number of
 * embedded resolvers to produce the next event in the authentication flow.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InitialAuthenticationAttemptWebflowEventResolver extends AbstractCasWebflowEventResolver implements CasDelegatingWebflowEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialAuthenticationAttemptWebflowEventResolver.class);

    private final List<CasWebflowEventResolver> orderedResolvers = new ArrayList<>();

    private CasWebflowEventResolver selectiveResolver;

    public InitialAuthenticationAttemptWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                            final CentralAuthenticationService centralAuthenticationService,
                                                            final ServicesManager servicesManager,
                                                            final TicketRegistrySupport ticketRegistrySupport,
                                                            final CookieGenerator warnCookieGenerator,
                                                            final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                            final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService,
                servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        try {
            final Credential credential = getCredentialFromContext(context);
            final Service service = WebUtils.getService(context);

            if (credential != null) {
                final AuthenticationResultBuilder builder = this.authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credential);
                if (builder.getInitialAuthentication().isPresent()) {
                    WebUtils.putAuthenticationResultBuilder(builder, context);
                    WebUtils.putAuthentication(builder.getInitialAuthentication().get(), context);
                }
            }

            final RegisteredService registeredService = determineRegisteredServiceForEvent(context, service);
            LOGGER.debug("Attempting to resolve candidate authentication events for service [{}]", service);
            final Set<Event> resolvedEvents = resolveCandidateAuthenticationEvents(context, service, registeredService);
            if (!resolvedEvents.isEmpty()) {
                LOGGER.debug("The set of authentication events resolved for [{}] are [{}]. Beginning to select the final event...", service, resolvedEvents);
                putResolvedEventsAsAttribute(context, resolvedEvents);
                final Event finalResolvedEvent = this.selectiveResolver.resolveSingle(context);
                LOGGER.debug("The final authentication event resolved for [{}] is [{}]", service, finalResolvedEvent);
                if (finalResolvedEvent != null) {
                    return CollectionUtils.wrapSet(finalResolvedEvent);
                }
            }


            final AuthenticationResultBuilder builder = WebUtils.getAuthenticationResultBuilder(context);
            if (builder == null) {
                throw new IllegalArgumentException("No authentication result builder can be located in the context");
            }
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            Event event = returnAuthenticationExceptionEventIfNeeded(e);
            if (event == null) {
                LOGGER.warn(e.getMessage(), e);
                event = newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, e);
            }
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return CollectionUtils.wrapSet(event);
        }
    }

    private RegisteredService determineRegisteredServiceForEvent(final RequestContext context, final Service service) {
        RegisteredService registeredService = null;
        if (service != null) {
            LOGGER.debug("Locating service [{}] in service registry to determine authentication policy", service);
            registeredService = this.servicesManager.findServiceBy(service);

            LOGGER.debug("Locating authentication event in the request context...");
            final Authentication authn = WebUtils.getAuthentication(context);
            LOGGER.debug("Enforcing access strategy policies for registered service [{}] and principal [{}]", registeredService, authn.getPrincipal());
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authn, false);
        }
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
    protected Set<Event> resolveCandidateAuthenticationEvents(final RequestContext context, final Service service, final RegisteredService registeredService) {
        return this.orderedResolvers
                .stream()
                .map(resolver -> resolver.resolveSingle(context))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
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

    public void setSelectiveResolver(final CasWebflowEventResolver r) {
        this.selectiveResolver = r;
    }

    private Event returnAuthenticationExceptionEventIfNeeded(final Exception e) {
        final Exception ex;
        if (e instanceof AuthenticationException || e instanceof AbstractTicketException) {
            ex = e;
        } else if (e.getCause() instanceof AuthenticationException || e.getCause() instanceof AbstractTicketException) {
            ex = (Exception) e.getCause();
        } else {
            return null;
        }

        LOGGER.debug(ex.getMessage(), ex);
        return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, ex);
    }
}
