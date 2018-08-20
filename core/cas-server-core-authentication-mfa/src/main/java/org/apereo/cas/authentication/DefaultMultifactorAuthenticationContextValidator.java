package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

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
public class DefaultMultifactorAuthenticationContextValidator implements AuthenticationContextValidator {

    private final String authenticationContextAttribute;
    private final String globalFailureMode;
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
     * If authentication event did bypass MFA, let for allow for validation to execute successfully.
     *
     * @param authentication   the authentication
     * @param requestedContext the requested context
     * @param service          the service
     * @return true if the context can be successfully validated.
     */
    @Override
    public Pair<Boolean, Optional<MultifactorAuthenticationProvider>> validate(final Authentication authentication,
                                                                               final String requestedContext,
                                                                               final RegisteredService service) {
        val attributes = authentication.getAttributes();
        val ctxAttr = attributes.get(this.authenticationContextAttribute);
        val contexts = CollectionUtils.toCollection(ctxAttr);
        LOGGER.debug("Attempting to match requested authentication context [{}] against [{}]", requestedContext, contexts);
        val providerMap =
            MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null) {
            LOGGER.debug("No multifactor authentication providers are configured");
            return Pair.of(Boolean.FALSE, Optional.empty());
        }
        val requestedProvider = locateRequestedProvider(providerMap.values(), requestedContext);
        if (!requestedProvider.isPresent()) {
            LOGGER.debug("Requested authentication provider cannot be recognized.");
            return Pair.of(Boolean.FALSE, Optional.empty());
        }
        if (contexts.stream().filter(ctx -> ctx.toString().equals(requestedContext)).count() > 0) {
            LOGGER.debug("Requested authentication context [{}] is satisfied", requestedContext);
            return Pair.of(Boolean.TRUE, requestedProvider);
        }
        if (StringUtils.isNotBlank(this.mfaTrustedAuthnAttributeName) && attributes.containsKey(this.mfaTrustedAuthnAttributeName)) {
            LOGGER.debug("Requested authentication context [{}] is satisfied since device is already trusted", requestedContext);
            return Pair.of(Boolean.TRUE, requestedProvider);
        }
        if (attributes.containsKey(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA)
            && attributes.containsKey(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER)) {
            val isBypass = Boolean.class.cast(attributes.get(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA));
            val bypassedId = attributes.get(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER).toString();
            LOGGER.debug("Found multifactor authentication bypass attributes for provider [{}]", bypassedId);
            if (isBypass && StringUtils.equals(bypassedId, requestedContext)) {
                LOGGER.debug("Requested authentication context [{}] is satisfied given mfa was bypassed for the authentication attempt", requestedContext);
                return Pair.of(Boolean.TRUE, requestedProvider);
            }
            LOGGER.debug("Either multifactor authentication was not bypassed or the requested context [{}] does not match the bypassed provider [{}]",
                requestedProvider, bypassedId);
        }
        val satisfiedProviders = getSatisfiedAuthenticationProviders(authentication, providerMap.values());
        if (satisfiedProviders != null && !satisfiedProviders.isEmpty()) {
            val providers = satisfiedProviders.toArray(new MultifactorAuthenticationProvider[]{});
            OrderComparator.sortIfNecessary(providers);
            val result = Arrays.stream(providers)
                .filter(provider -> {
                    val p = requestedProvider.get();
                    return provider.equals(p) || provider.getOrder() >= p.getOrder();
                })
                .findFirst();
            if (result.isPresent()) {
                LOGGER.debug("Current provider [{}] already satisfies the authentication requirements of [{}]; proceed with flow normally.",
                    result.get(), requestedProvider);
                return Pair.of(Boolean.TRUE, requestedProvider);
            }
        }
        return handleUnsatisfiedAuthenticationContext(requestedContext, service, requestedProvider, satisfiedProviders);
    }

    private Pair<Boolean, Optional<MultifactorAuthenticationProvider>> handleUnsatisfiedAuthenticationContext(final String requestedContext,
                                                                                                              final RegisteredService service,
                                                                                                              final Optional<MultifactorAuthenticationProvider> requestedProvider,
                                                                                                              final Collection<MultifactorAuthenticationProvider> satisfiedProviders) {
        val provider = requestedProvider.get();
        LOGGER.debug("No multifactor providers could be located to satisfy the requested context for [{}]", provider);
        val mode = getMultifactorFailureModeForService(service);
        if (mode == RegisteredServiceMultifactorPolicy.FailureModes.PHANTOM) {
            if (!provider.isAvailable(service)) {
                LOGGER.debug("Service [{}] is configured to use a [{}] failure mode for multifactor authentication policy. "
                    + "Since provider [{}] is unavailable at the moment, CAS will knowingly allow [{}] as a satisfied criteria "
                    + "of the present authentication context", service.getServiceId(), mode, requestedProvider, requestedContext);
                return Pair.of(Boolean.TRUE, requestedProvider);
            }
        }
        if (mode == RegisteredServiceMultifactorPolicy.FailureModes.OPEN) {
            if (!provider.isAvailable(service)) {
                LOGGER.debug("Service [{}] is configured to use a [{}] failure mode for multifactor authentication policy and "
                    + "since provider [{}] is unavailable at the moment, CAS will consider the authentication satisfied "
                    + "without the presence of [{}]", service.getServiceId(), mode, requestedProvider, requestedContext);
                return Pair.of(Boolean.TRUE, Optional.empty());
            }
        }
        return Pair.of(Boolean.FALSE, requestedProvider);
    }

    private Collection<MultifactorAuthenticationProvider> getSatisfiedAuthenticationProviders(final Authentication authentication,
                                                                                              final Collection<MultifactorAuthenticationProvider> providers) {
        val contexts = CollectionUtils.toCollection(authentication.getAttributes().get(this.authenticationContextAttribute));
        if (contexts == null || contexts.isEmpty()) {
            LOGGER.debug("No authentication context could be determined based on authentication attribute [{}]", this.authenticationContextAttribute);
            return null;
        }
        contexts.forEach(context -> providers.removeIf(provider -> !provider.getId().equals(context)));
        LOGGER.debug("Found [{}] providers that may satisfy the context", providers.size());
        return providers;
    }

    private RegisteredServiceMultifactorPolicy.FailureModes getMultifactorFailureModeForService(final RegisteredService service) {
        val policy = service.getMultifactorPolicy();
        if (policy == null || policy.getFailureMode() == null || policy.getFailureMode().equals(RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED)) {
            return RegisteredServiceMultifactorPolicy.FailureModes.valueOf(this.globalFailureMode);
        }
        return policy.getFailureMode();
    }
}
