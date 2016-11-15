package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SelectiveAuthenticationProviderWebflowEventResolver}
 * that acts as a stub resolver, specifically designed for extensions.
 * Deployers can extend this class to perform additional processes on the final set
 * of resolved events, to select one vs another based on the nature of the event attributes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SelectiveAuthenticationProviderWebflowEventResolver extends BaseMultifactorAuthenticationProviderResolver {

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final Set<Event> resolvedEvents = getResolvedEventsAsAttribute(context);
        final Authentication authentication = WebUtils.getAuthentication(context);
        final RegisteredService registeredService = WebUtils.getRegisteredService(context);
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
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
    protected Set<Event> resolveEventsInternal(final Set<Event> resolveEvents,
                                               final Authentication authentication,
                                               final RegisteredService registeredService,
                                               final HttpServletRequest request,
                                               final RequestContext context) {
        logger.debug("Collection of resolved events for this authentication sequence are:");
        resolveEvents.forEach(e -> logger.debug("Event id [{}] resolved from {}", e.getId(), e.getSource().getClass().getName()));
        final Pair<Set<Event>, Collection<MultifactorAuthenticationProvider>> pair =
                filterEventsByMultifactorAuthenticationProvider(resolveEvents, authentication, registeredService);
        WebUtils.putResolvedMultifactorAuthenticationProviders(context, pair.getValue());
        return pair.getKey();
    }

    /**
     * Filter events by multifactor authentication providers.
     *
     * @param resolveEvents     the resolve events
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @return the set of events
     */
    protected Pair<Set<Event>, Collection<MultifactorAuthenticationProvider>> filterEventsByMultifactorAuthenticationProvider(
            final Set<Event> resolveEvents, final Authentication authentication,
            final RegisteredService registeredService) {
        logger.debug("Locating multifactor providers to determine support for this authentication sequence");
        final Map<String, MultifactorAuthenticationProvider> providers =
                WebUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);

        if (providers == null || providers.isEmpty()) {
            logger.debug("No providers are available to honor this request. Moving on...");
            return Pair.of(resolveEvents, Sets.newHashSet());
        }

        final Collection<MultifactorAuthenticationProvider> flattenedProviders = flattenProviders(providers.values());

        // remove providers that don't support the event
        flattenedProviders.removeIf(p -> resolveEvents.stream().filter(e -> p.supports(e, authentication, registeredService)).count() == 0);

        // remove events that are not supported by providers.
        resolveEvents.removeIf(e -> flattenedProviders.stream().filter(p -> p.supports(e, authentication, registeredService)).count() == 0);

        logger.debug("Finalized set of resolved events are {}", resolveEvents);
        return Pair.of(resolveEvents, flattenedProviders);
    }
}
