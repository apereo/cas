package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.Event;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link RegisteredServiceMappedPrincipalAttributeToMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceMappedPrincipalAttributeToMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;

    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;

    private final ApplicationContext applicationContext;

    private final TenantExtractor tenantExtractor;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   @Nullable final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   @Nullable final Service service)
            throws Throwable {
        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }

        LOGGER.trace("Evaluating multifactor authentication policy for registered service [{}]", registeredService);
        val policy = registeredService.getMultifactorAuthenticationPolicy();
        if (policy == null || policy.getMultifactorProviderSelectionMappings() == null) {
            LOGGER.trace("Authentication policy is absent or does not contain any multifactor authentication provider mappings");
            return Optional.empty();
        }

        if (StringUtils.isBlank(policy.getPrincipalAttributeNameTrigger())) {
            LOGGER.debug("Authentication policy does not define a principal attribute");
            return Optional.empty();
        }

        val providerMappings = policy.getMultifactorProviderSelectionMappings();
        if (providerMappings.isEmpty()) {
            LOGGER.debug("Authentication policy does not define any multifactor authentication provider mappings");
            return Optional.empty();
        }
        if (policy.getMultifactorAuthenticationProviders() != null && !policy.getMultifactorAuthenticationProviders().isEmpty()) {
            LOGGER.warn("Multifactor providers explicitly defined may not be defined with mapped providers. Skipping...");
            return Optional.empty();
        }

        val mappedProviders = providerMappings.keySet()
            .stream()
            .map(p -> MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(p, applicationContext))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        val principal = multifactorAuthenticationProviderResolver.resolvePrincipal(authentication.getPrincipal());

        val result = multifactorAuthenticationProviderResolver.resolveEventViaPrincipalAttribute(principal,
            org.springframework.util.StringUtils.commaDelimitedListToSet(policy.getPrincipalAttributeNameTrigger()),
            registeredService, service, Optional.empty(), mappedProviders,
            (attributeValue, mfaProvider) -> {
                val mappedAttributeValue = providerMappings.get(mfaProvider.getId());
                return StringUtils.isNotBlank(attributeValue)
                    && Objects.nonNull(mappedAttributeValue)
                    && RegexUtils.find(mappedAttributeValue.toString(), attributeValue);
            });

        if (result != null && !result.isEmpty()) {
            return CollectionUtils.firstElement(result)
                .map(Event.class::cast)
                .flatMap(event -> mappedProviders
                    .stream()
                    .filter(provider -> provider.getId().equals(event.getId()))
                    .findFirst())
                .or(() -> unmatchedMultifactorAuthenticationTrigger(principal, registeredService));
        }

        return unmatchedMultifactorAuthenticationTrigger(principal, registeredService);
    }

    private Optional<MultifactorAuthenticationProvider> unmatchedMultifactorAuthenticationTrigger(final Principal principal,
                                                                                                  final RegisteredService registeredService) {
        if (casProperties.getAuthn().getMfa().getTriggers().getPrincipal().isDenyIfUnmatched()) {
            throw new AuthenticationException(new MultifactorAuthenticationRequiredException(registeredService, principal));
        }
        return Optional.empty();
    }
}
