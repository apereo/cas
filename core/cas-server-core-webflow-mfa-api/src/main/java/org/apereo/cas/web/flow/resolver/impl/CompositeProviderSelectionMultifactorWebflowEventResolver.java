package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidationResult;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.webflow.execution.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link CompositeProviderSelectionMultifactorWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class CompositeProviderSelectionMultifactorWebflowEventResolver extends SelectiveMultifactorAuthenticationProviderWebflowEventResolver {

    public CompositeProviderSelectionMultifactorWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    protected Pair<Collection<Event>, Collection<MultifactorAuthenticationProvider>> filterEventsByMultifactorAuthenticationProvider(
        final Collection<Event> resolveEvents,
        final Authentication authentication,
        final RegisteredService registeredService,
        final HttpServletRequest request) {

        val composite = resolveEvents
            .stream()
            .allMatch(event -> event.getId().equalsIgnoreCase(ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER));
        if (!composite) {
            return super.filterEventsByMultifactorAuthenticationProvider(resolveEvents, authentication, registeredService, request);
        }
        val event = resolveEvents.iterator().next();
        val chainingProvider = (ChainingMultifactorAuthenticationProvider)
            event.getAttributes().get(MultifactorAuthenticationProvider.class.getName());

        return chainingProvider.getMultifactorAuthenticationProviders()
            .stream()
            .map(provider -> getConfigurationContext().getAuthenticationContextValidator()
                .validate(authentication, provider.getId(), Optional.ofNullable(registeredService)))
            .filter(MultifactorAuthenticationContextValidationResult::isSuccess)
            .map(result -> {
                val validatedProvider = result.getProvider().orElseThrow();
                val validatedEvent = CollectionUtils.wrapCollection(new Event(this,
                    validatedProvider.getId(), event.getAttributes()));
                val validatedProviders = CollectionUtils.wrapCollection(validatedProvider);
                return Pair.of(validatedEvent, validatedProviders);
            })
            .findAny()
            .orElseGet(() -> {
                LOGGER.debug("Finalized set of resolved events are [{}]", resolveEvents);
                return Pair.of(resolveEvents, chainingProvider.getMultifactorAuthenticationProviders());
            });
    }
}
