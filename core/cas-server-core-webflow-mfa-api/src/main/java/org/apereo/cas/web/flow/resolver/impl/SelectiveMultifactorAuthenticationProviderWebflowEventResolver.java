package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is {@link SelectiveMultifactorAuthenticationProviderWebflowEventResolver}
 * that acts as a stub resolver, specifically designed for extensions.
 * Deployers can extend this class to perform additional processes on the final set
 * of resolved events, to select one vs another based on the nature of the event attributes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SelectiveMultifactorAuthenticationProviderWebflowEventResolver
    extends BaseMultifactorAuthenticationProviderEventResolver {

    public SelectiveMultifactorAuthenticationProviderWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext) {
        super(webflowEventResolutionConfigurationContext);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val resolvedEvents = WebUtils.getResolvedEventsAsAttribute(context);
        val authentication = WebUtils.getAuthentication(context);
        val registeredService = resolveRegisteredServiceInRequestContext(context);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        return resolveEventsInternal(resolvedEvents, authentication, registeredService, request, context);
    }

    /**
     * Resolve events internal set. Implementation may filter events from the collection
     * to only return the one that is appropriate for this request. The default
     * implementation returns the entire collection.
     *
     * @param resolveEvents     the resolve events
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param request           the request
     * @param context           the request context
     * @return the set of resolved events
     */
    protected Set<Event> resolveEventsInternal(final Collection<Event> resolveEvents,
                                               final Authentication authentication,
                                               final RegisteredService registeredService,
                                               final HttpServletRequest request,
                                               final RequestContext context) {
        if (!resolveEvents.isEmpty()) {
            LOGGER.trace("Collection of resolved events for this authentication sequence are:");
            resolveEvents.forEach(e -> LOGGER.trace("Event id [{}] resolved from [{}]",
                e.getId(), e.getSource().getClass().getName()));
        } else {
            LOGGER.trace("No events resolved for authentication transaction [{}] and service [{}]",
                authentication, registeredService);
        }
        val pair = filterEventsByMultifactorAuthenticationProvider(resolveEvents, authentication, registeredService, request);
        WebUtils.putResolvedMultifactorAuthenticationProviders(context, pair.getValue());
        return new HashSet<>(pair.getKey());
    }

    /**
     * Filter events by multifactor authentication providers.
     *
     * @param resolveEvents     the resolve events
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param request           the request
     * @return the set of events
     */
    protected Pair<Collection<Event>, Collection<MultifactorAuthenticationProvider>> filterEventsByMultifactorAuthenticationProvider(
        final Collection<Event> resolveEvents,
        final Authentication authentication,
        final RegisteredService registeredService,
        final HttpServletRequest request) {

        LOGGER.debug("Locating multifactor providers to determine support for this authentication sequence");
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(
            getWebflowEventResolutionConfigurationContext().getApplicationContext());

        if (providers.isEmpty()) {
            LOGGER.debug("No providers are available to honor this request. Moving on...");
            return Pair.of(resolveEvents, new HashSet<>(0));
        }

        val providerValues = providers.values();

        providerValues.removeIf(p -> resolveEvents.stream().noneMatch(e -> p.matches(e.getId())));
        resolveEvents.removeIf(e -> providerValues.stream().noneMatch(p -> p.matches(e.getId())));

        LOGGER.debug("Finalized set of resolved events are [{}]", resolveEvents);
        val finalEvents = new TreeSet<>(Comparator.comparing(Event::getId));
        finalEvents.addAll(resolveEvents);
        return Pair.of(finalEvents, providerValues);
    }
}
