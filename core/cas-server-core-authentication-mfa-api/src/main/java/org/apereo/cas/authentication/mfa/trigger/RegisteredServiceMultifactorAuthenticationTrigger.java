package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
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
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;
    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final Service service) {
        if (registeredService == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }

        val policy = registeredService.getMultifactorPolicy();
        if (policy == null || policy.getMultifactorAuthenticationProviders().isEmpty()) {
            LOGGER.trace("Authentication policy does not contain any multifactor authentication providers");
            return Optional.empty();
        }

        if (StringUtils.isNotBlank(policy.getPrincipalAttributeNameTrigger())
            || StringUtils.isNotBlank(policy.getPrincipalAttributeValueToMatch())) {
            LOGGER.debug("Authentication policy for [{}] has defined principal attribute triggers. Skipping...",
                registeredService.getServiceId());
            return Optional.empty();
        }

        val principal = authentication.getPrincipal();
        val providers = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService);
        if (providers != null && !providers.isEmpty()) {
            val provider = multifactorAuthenticationProviderSelector.resolve(providers, registeredService, principal);
            LOGGER.debug("Selected multifactor authentication provider for this transaction is [{}]", provider);
            if (provider != null) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }
}
