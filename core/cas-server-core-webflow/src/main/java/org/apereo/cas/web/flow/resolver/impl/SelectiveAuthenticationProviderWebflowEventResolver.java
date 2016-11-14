package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link SelectiveAuthenticationProviderWebflowEventResolver}
 * that acts as a stub resolver, specifically designed for extensions.
 * Deployers can extend this class to perform additional processes on the final set
 * of resolved events, to select one vs another based on the nature of the event attributes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SelectiveAuthenticationProviderWebflowEventResolver extends AbstractCasWebflowEventResolver {
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
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
        for (final Event resolveEvent : resolveEvents) {
            logger.debug("Event id [{}] resolved from {}",
                    resolveEvent.getId(), resolveEvent.getSource().getClass().getName());
        }
        return filterEventsByMultifactorAuthenticationProvider(resolveEvents, authentication, registeredService);
    }

    /**
     * Filter events by multifactor authentication providers.
     *
     * @param resolveEvents     the resolve events
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @return the set of events
     */
    protected Set<Event> filterEventsByMultifactorAuthenticationProvider(final Set<Event> resolveEvents,
                                                                         final Authentication authentication,
                                                                         final RegisteredService registeredService) {
        logger.debug("Locating multifactor providers to determine support for this authentication sequence");
        final Map<String, MultifactorAuthenticationProvider> providers =
                WebUtils.getAllMultifactorAuthenticationProviders(applicationContext);

        if (providers == null || providers.isEmpty()) {
            logger.debug("No providers are available to honor this request. Moving on...");
            return resolveEvents;
        }

        final Set<Event> finalEvents = resolveEvents
                .stream()
                .filter(e -> !providers.entrySet()
                        .stream()
                        .filter(p -> p.getValue().supports(e, authentication, registeredService))
                        .collect(Collectors.toSet())
                        .isEmpty())
                .collect(Collectors.toSet());

        logger.debug("Finalized set of resolved events are {}", finalEvents);
        return finalEvents;
    }
}
