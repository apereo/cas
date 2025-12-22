package org.apereo.cas.web.flow.resolver.impl;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;

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
    public Set<Event> resolveInternal(final RequestContext context) throws Throwable {
        val resolvedEvents = WebUtils.getResolvedEventsAsAttribute(context);
        val authentication = WebUtils.getAuthentication(context);
        val service = WebUtils.getService(context);
        val registeredService = resolveRegisteredServiceInRequestContext(context);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        return resolveEventsInternal(resolvedEvents, authentication, registeredService, request, context, service);
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
     * @param service           the service
     * @return the set of resolved events
     */
    protected Set<Event> resolveEventsInternal(final Collection<Event> resolveEvents,
                                               final Authentication authentication,
                                               @Nullable final RegisteredService registeredService,
                                               final HttpServletRequest request,
                                               final RequestContext context,
                                               @Nullable final Service service) throws Throwable {
        if (resolveEvents.isEmpty()) {
            LOGGER.trace("No events resolved for authentication transaction [{}] and service [{}]",
                authentication, registeredService);
        } else {
            LOGGER.trace("Collection of resolved events for this authentication sequence are:");
            resolveEvents.forEach(e -> LOGGER.trace("Event id [{}] resolved from [{}]",
                e.getId(), e.getSource().getClass().getName()));
        }
        val result = filterEventsByMultifactorAuthenticationProvider(resolveEvents, authentication, registeredService, request, service);
        return result.map(pair -> {
            MultifactorAuthenticationWebflowUtils.putResolvedMultifactorAuthenticationProviders(context, pair.getValue());
            return new HashSet<>(pair.getKey());
        }).orElseGet(HashSet::new);
    }

    protected Optional<Pair<Collection<Event>, Collection<MultifactorAuthenticationProvider>>> filterEventsByMultifactorAuthenticationProvider(
        final Collection<Event> resolveEvents,
        final Authentication authentication,
        @Nullable final RegisteredService registeredService,
        final HttpServletRequest request,
        @Nullable final Service service) throws Throwable {

        LOGGER.debug("Locating multifactor providers to determine support for this authentication sequence");
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(
            getConfigurationContext().getApplicationContext());

        if (providers.isEmpty()) {
            LOGGER.debug("No providers are available to honor this request. Moving on...");
            return Optional.of(Pair.of(resolveEvents, new HashSet<>()));
        }

        val providerValues = providers.values();

        providerValues.removeIf(p -> resolveEvents.stream().noneMatch(e -> p.matches(e.getId())));
        resolveEvents.removeIf(e -> providerValues.stream().noneMatch(p -> p.matches(e.getId())));

        LOGGER.debug("Finalized set of resolved events are [{}]", resolveEvents);
        val selectedProvider = Objects.requireNonNull(getConfigurationContext().getMultifactorAuthenticationProviderSelector()
            .resolve(providerValues, registeredService, authentication.getPrincipal()));
        val finalEvent = resolveEvents
            .stream()
            .filter(event -> selectedProvider.matches(event.getId())).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unable to locate multifactor authentication provider by id " + selectedProvider.getId()));
        return Optional.of(Pair.of(List.of(finalEvent), List.of(selectedProvider)));
    }
}
