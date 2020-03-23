package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link GlobalMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class GlobalMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;

    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final Service service) {

        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val globalProviderId = casProperties.getAuthn().getMfa().getGlobalProviderId();
        if (StringUtils.isBlank(globalProviderId)) {
            LOGGER.trace("No value could be found for for the global provider id");
            return Optional.empty();
        }
        LOGGER.debug("Attempting to globally activate [{}]", globalProviderId);

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context to handle [{}]", globalProviderId);
            throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        }

        val providers = org.springframework.util.StringUtils.commaDelimitedListToSet(globalProviderId);
        val resolvedProviders = providers.stream()
            .map(provider -> MultifactorAuthenticationUtils.resolveProvider(providerMap, provider))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparing(MultifactorAuthenticationProvider::getOrder))
            .collect(Collectors.toList());

        if (resolvedProviders.size() != providers.size()) {
            val providerIds = resolvedProviders
                .stream()
                .map(MultifactorAuthenticationProvider::getId)
                .collect(Collectors.joining(","));
            val message = String.format("Not all requested multifactor providers could be found. "
                    + "Requested providers are [%s] and resolved providers are [%s]", globalProviderId, providerIds);
            LOGGER.warn(message, globalProviderId);
            throw new MultifactorAuthenticationProviderAbsentException(message);
        }

        if (resolvedProviders.size() == 1) {
            val provider = resolvedProviders.get(0);
            LOGGER.debug("Resolved single multifactor provider [{}]", provider);
            return Optional.of(provider);
        }

        val principal = authentication.getPrincipal();
        val provider = multifactorAuthenticationProviderSelector.resolve(resolvedProviders, registeredService, principal);
        LOGGER.debug("Selected multifactor authentication provider for this transaction is [{}]", provider);
        if (provider != null) {
            return Optional.of(provider);
        }
        return Optional.empty();
    }
}
