package org.apereo.cas.web.flow.resolver.impl;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidationResult;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CompositeProviderSelectionMultifactorWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class CompositeProviderSelectionMultifactorWebflowEventResolver extends SelectiveMultifactorAuthenticationProviderWebflowEventResolver {
    protected final CasCookieBuilder cookieBuilder;

    public CompositeProviderSelectionMultifactorWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext configurationContext,
        final CasCookieBuilder cookieBuilder) {
        super(configurationContext);
        this.cookieBuilder = cookieBuilder;
    }

    @Override
    protected Optional<Pair<Collection<Event>, Collection<MultifactorAuthenticationProvider>>> filterEventsByMultifactorAuthenticationProvider(
        final Collection<Event> resolveEvents, final Authentication authentication,
        @Nullable final RegisteredService registeredService, final HttpServletRequest request,
        @Nullable final Service service) throws Throwable {

        val composite = resolveEvents
            .stream()
            .allMatch(event -> event.getId().equalsIgnoreCase(ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER));
        if (!composite) {
            return super.filterEventsByMultifactorAuthenticationProvider(resolveEvents, authentication, registeredService, request, service);
        }
        val event = resolveEvents.iterator().next();
        val chainingProvider = (ChainingMultifactorAuthenticationProvider)
            event.getAttributes().get(MultifactorAuthenticationProvider.class.getName());

        val selectedMfaProvider = cookieBuilder.retrieveCookieValue(request);
        val selectedProviders = chainingProvider.getMultifactorAuthenticationProviders()
            .stream()
            .filter(provider -> StringUtils.isBlank(selectedMfaProvider) || provider.matches(selectedMfaProvider))
            .map(provider -> getConfigurationContext().getAuthenticationContextValidator()
                .validate(authentication, provider.getId(), Optional.ofNullable(registeredService)))
            .filter(MultifactorAuthenticationContextValidationResult::isSuccess)
            .map(result -> {
                val validatedProvider = result.getProvider().orElseThrow();
                val validatedEvent = CollectionUtils.wrapCollection(new Event(this,
                    validatedProvider.getId(), event.getAttributes()));
                val validatedProviders = CollectionUtils.wrapCollection(validatedProvider);
                return Optional.of(Pair.of(validatedEvent, validatedProviders));
            })
            .findAny()
            .orElseGet(() -> {
                val activeProviders = chainingProvider.getMultifactorAuthenticationProviders()
                    .stream()
                    .filter(provider -> StringUtils.isBlank(selectedMfaProvider) || provider.matches(selectedMfaProvider))
                    .filter(provider -> {
                        val bypass = provider.getBypassEvaluator();
                        return bypass == null || bypass.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request, service);
                    })
                    .collect(Collectors.toList());
                LOGGER.debug("Finalized set of resolved events are [{}] with providers [{}]", resolveEvents, activeProviders);
                return activeProviders.isEmpty() ? Optional.empty() : Optional.of(Pair.of(resolveEvents, activeProviders));
            });

        if (selectedProviders.isPresent() && StringUtils.isNotBlank(selectedMfaProvider)) {
            val resolvedProviders = selectedProviders.get().getValue();
            if (resolvedProviders.size() == 1) {
                val rememberedProvider = resolvedProviders.stream().filter(provider -> provider.matches(selectedMfaProvider))
                    .findFirst().orElseThrow();
                return Optional.of(Pair.of(Set.of(new Event(this, selectedMfaProvider)),
                    CollectionUtils.wrapArrayList(rememberedProvider)));
            }
        }
        return selectedProviders;
    }
}
