package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.Event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

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

    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    private final TenantExtractor tenantExtractor;
    
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   final Service service) throws Throwable {
        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }

        LOGGER.trace("Evaluating multifactor authentication policy for registered service [{}]", registeredService);
        val policy = registeredService.getMultifactorAuthenticationPolicy();
        if (policy == null || registeredService.getMultifactorAuthenticationPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            LOGGER.trace("Authentication policy is absent or does not contain any multifactor authentication providers");
            return Optional.empty();
        }

        if (StringUtils.isBlank(policy.getPrincipalAttributeNameTrigger())
            || StringUtils.isBlank(policy.getPrincipalAttributeValueToMatch())) {
            LOGGER.debug("Authentication policy does not define a principal attribute and/or value to trigger multifactor authentication");
            return Optional.empty();
        }

        val principal = multifactorAuthenticationProviderResolver.resolvePrincipal(authentication.getPrincipal());
        val providers = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService, applicationContext);
        if (providers.size() > 1) {
            val resolvedProvider = multifactorAuthenticationProviderSelector.resolve(providers, registeredService, principal);
            providers.clear();
            providers.add(resolvedProvider);
        }
        LOGGER.debug("Resolved multifactor providers are [{}]", providers);
        val result = multifactorAuthenticationProviderResolver.resolveEventViaPrincipalAttribute(principal,
            org.springframework.util.StringUtils.commaDelimitedListToSet(policy.getPrincipalAttributeNameTrigger()),
            registeredService, service, Optional.empty(), providers,
            (attributeValue, mfaProvider) ->
                attributeValue != null && RegexUtils.find(policy.getPrincipalAttributeValueToMatch(), attributeValue));

        if (result != null && !result.isEmpty()) {
            return CollectionUtils.firstElement(result)
                .map(Event.class::cast)
                .map(event -> {
                    val provider = CollectionUtils.firstElement(providers, MultifactorAuthenticationProvider.class).orElseThrow();
                    if (provider instanceof final ChainingMultifactorAuthenticationProvider chain && provider.getId().equals(event.getId())) {
                        val matched = chain.getMultifactorAuthenticationProviders()
                            .stream()
                            .map(p -> MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(p.getId(), applicationContext))
                            .allMatch(Optional::isPresent);
                        return matched ? Optional.of(provider) : unmatchedMultifactorAuthenticationTrigger(principal, registeredService);
                    }
                    return MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(event.getId(), applicationContext);
                })
                .orElseGet(() -> unmatchedMultifactorAuthenticationTrigger(principal, registeredService));
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
