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
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    private final TenantExtractor tenantExtractor;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest request,
                                                                   final HttpServletResponse response,
                                                                   final Service service) throws Throwable {

        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val globalProviderIds = findGlobalProviderIds(request);
        if (globalProviderIds == null || globalProviderIds.isEmpty()) {
            LOGGER.trace("No value could be found for for the global provider id");
            return Optional.empty();
        }
        LOGGER.debug("Attempting to globally activate [{}]", globalProviderIds);
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context to handle [{}]", globalProviderIds);
            throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        }

        val resolvedProviders = globalProviderIds.stream()
            .map(provider -> MultifactorAuthenticationUtils.resolveProvider(providerMap, provider))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparing(MultifactorAuthenticationProvider::getOrder))
            .collect(Collectors.toList());

        if (resolvedProviders.size() != globalProviderIds.size()) {
            handleAbsentMultifactorProvider(globalProviderIds, resolvedProviders);
        }

        if (resolvedProviders.size() == 1) {
            return resolveSingleMultifactorProvider(resolvedProviders.getFirst());
        }

        return resolveMultifactorProvider(authentication, registeredService, resolvedProviders);
    }

    protected Set<String> findGlobalProviderIds(final HttpServletRequest httpServletRequest) {
        return tenantExtractor.extract(httpServletRequest)
            .map(TenantDefinition::getProperties)
            .flatMap(CasConfigurationProperties::bindFrom)
            .map(properties -> {
                val globalProviderId = properties.getAuthn().getMfa().getTriggers().getGlobal().getGlobalProviderId();
                return StringUtils.commaDelimitedListToSet(globalProviderId);
            })
            .filter(providers -> !providers.isEmpty())
            .orElseGet(() -> {
                val globalProviderId = casProperties.getAuthn().getMfa().getTriggers().getGlobal().getGlobalProviderId();
                return StringUtils.commaDelimitedListToSet(globalProviderId);
            });
    }

    protected void handleAbsentMultifactorProvider(final Set<String> globalProviderIds,
                                                   final List<MultifactorAuthenticationProvider> resolvedProviders) {
        val providerIds = resolvedProviders
            .stream()
            .map(MultifactorAuthenticationProvider::getId)
            .collect(Collectors.joining(","));
        val message = String.format("Not all requested multifactor providers could be found. "
            + "Requested providers are [%s] and resolved providers are [%s]", globalProviderIds, providerIds);
        LOGGER.warn(message, globalProviderIds);
        throw new MultifactorAuthenticationProviderAbsentException(message);
    }

    protected Optional<MultifactorAuthenticationProvider> resolveSingleMultifactorProvider(
        final MultifactorAuthenticationProvider resolvedProvider) {
        LOGGER.debug("Resolved single multifactor provider [{}]", resolvedProvider);
        return Optional.of(resolvedProvider);
    }

    protected Optional<MultifactorAuthenticationProvider> resolveMultifactorProvider(
        final Authentication authentication,
        final RegisteredService registeredService,
        final List<MultifactorAuthenticationProvider> resolvedProviders) throws Throwable {
        val principal = authentication.getPrincipal();
        val provider = multifactorAuthenticationProviderSelector.resolve(resolvedProviders, registeredService, principal);
        LOGGER.debug("Selected multifactor authentication provider for this transaction is [{}]", provider);
        return Optional.ofNullable(provider);
    }
}
