package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;
    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;
    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final Service service) {
        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }

        val policy = registeredService.getMultifactorPolicy();
        if (policy == null || registeredService.getMultifactorPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            LOGGER.trace("Authentication policy is absent or does not contain any multifactor authentication providers");
            return Optional.empty();
        }

        if (StringUtils.isBlank(policy.getPrincipalAttributeNameTrigger())
            || StringUtils.isBlank(policy.getPrincipalAttributeValueToMatch())) {
            LOGGER.debug("Authentication policy does not define a principal attribute and/or value to trigger multifactor authentication");
            return Optional.empty();
        }

        val principal = authentication.getPrincipal();
        val providers = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService);
        val result = multifactorAuthenticationProviderResolver.resolveEventViaPrincipalAttribute(principal,
            org.springframework.util.StringUtils.commaDelimitedListToSet(policy.getPrincipalAttributeNameTrigger()),
            registeredService, Optional.empty(), providers,
            (attributeValue, mfaProvider) -> attributeValue != null && RegexUtils.matches(Pattern.compile(policy.getPrincipalAttributeValueToMatch()), attributeValue));

        if (result != null && !result.isEmpty()) {
            val id = CollectionUtils.firstElement(result);
            if (id.isEmpty()) {
                return Optional.empty();
            }
            return MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(id.get().toString(), this.applicationContext);
        }
        return Optional.empty();
    }
}
