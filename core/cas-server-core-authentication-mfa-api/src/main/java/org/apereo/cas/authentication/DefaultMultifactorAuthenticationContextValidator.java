package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The {@link DefaultMultifactorAuthenticationContextValidator} is responsible for evaluating an authentication
 * object to see whether it satisfied a requested authentication context.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultMultifactorAuthenticationContextValidator implements MultifactorAuthenticationContextValidator {

    private final String authenticationContextAttribute;

    private final String mfaTrustedAuthnAttributeName;

    private final ConfigurableApplicationContext applicationContext;

    private static Optional<MultifactorAuthenticationProvider> locateRequestedProvider(
        final Collection<MultifactorAuthenticationProvider> providersArray, final String requestedProvider) {
        return providersArray.stream().filter(provider -> provider.getId().equals(requestedProvider)).findFirst();
    }

    /**
     * {@inheritDoc}
     * If the authentication event is established as part trusted/device browser
     * such that MFA was skipped, allow for validation to execute successfully.
     * If authentication event did bypass MFA, allow for validation to execute successfully.
     *
     * @param authentication   the authentication
     * @param requestedContext the requested context
     * @param service          the service
     * @return true if the context can be successfully validated.
     */
    @Override
    public MultifactorAuthenticationContextValidationResult validate(final Authentication authentication,
                                                                    final String requestedContext,
                                                                    final Optional<RegisteredService> service) {
        val attributes = authentication.getAttributes();
        val ctxAttr = attributes.get(this.authenticationContextAttribute);
        val contexts = CollectionUtils.toCollection(ctxAttr);
        LOGGER.trace("Attempting to match requested authentication context [{}] against [{}]", requestedContext, contexts);
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        LOGGER.trace("Available MFA providers are [{}]", providerMap.values());
        
        String requestedContextTmp=requestedContext;
        if ("mfa-composite".equals(requestedContext)){
            requestedContextTmp=CollectionUtils.firstElement(contexts).get().toString();
            LOGGER.trace("MFA-Composite detected: set requested Context from Authentication Context [{}]", requestedContextTmp);
        }
        final String requestedContextFinal=requestedContextTmp;
        
        val requestedProvider = locateRequestedProvider(providerMap.values(), requestedContextFinal);
        if (requestedProvider.isEmpty()) {
            LOGGER.debug("Requested authentication provider cannot be recognized.");
            return MultifactorAuthenticationContextValidationResult.builder().success(false).build();
        }
        LOGGER.debug("Requested context is [{}] and available contexts are [{}]", requestedContextFinal, contexts);
        if (contexts.stream().anyMatch(ctx -> ctx.toString().equals(requestedContextFinal))) {
            LOGGER.debug("Requested authentication context [{}] is satisfied", requestedContextFinal);
            return MultifactorAuthenticationContextValidationResult.builder()
                .success(true).provider(requestedProvider).build();
        }
        if (StringUtils.isNotBlank(this.mfaTrustedAuthnAttributeName) && attributes.containsKey(this.mfaTrustedAuthnAttributeName)) {
            LOGGER.debug("Requested authentication context [{}] is satisfied since device is already trusted", requestedContextFinal);
            return MultifactorAuthenticationContextValidationResult.builder()
                .success(true).provider(requestedProvider).build();
        }
        val provider = requestedProvider.get();
        val satisfiedProviders = getSatisfiedAuthenticationProviders(authentication, providerMap.values());
        if (satisfiedProviders != null && !satisfiedProviders.isEmpty()) {
            val providers = satisfiedProviders.toArray(MultifactorAuthenticationProvider[]::new);
            OrderComparator.sortIfNecessary(providers);
            val result = Arrays.stream(providers)
                .filter(p -> p.equals(provider) || p.getOrder() >= provider.getOrder())
                .findFirst();
            if (result.isPresent()) {
                LOGGER.debug("Current provider [{}] already satisfies the authentication requirements of [{}]; proceed with flow normally.",
                    result.get(), requestedProvider);
                return MultifactorAuthenticationContextValidationResult.builder()
                    .success(true).provider(requestedProvider).build();
            }
        }
        LOGGER.debug("No multifactor providers could be located to satisfy the requested context for [{}]", provider);
        return MultifactorAuthenticationContextValidationResult.builder().success(false).provider(requestedProvider).build();
    }

    private Collection<MultifactorAuthenticationProvider> getSatisfiedAuthenticationProviders(final Authentication authentication,
                                                                                              final Collection<MultifactorAuthenticationProvider> providers) {
        val contexts = CollectionUtils.toCollection(authentication.getAttributes().get(this.authenticationContextAttribute));
        if (contexts == null || contexts.isEmpty()) {
            LOGGER.debug("No authentication context could be determined based on authentication attribute [{}]", this.authenticationContextAttribute);
            return null;
        }
        return providers.stream()
            .filter(p -> contexts.contains(p.getId()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
