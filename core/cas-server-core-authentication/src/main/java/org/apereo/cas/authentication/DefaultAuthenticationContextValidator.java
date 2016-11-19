package org.apereo.cas.authentication;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link DefaultAuthenticationContextValidator} is responsible for evaluating an authentication
 * object to see whether it satisfied a requested authentication context.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
public class DefaultAuthenticationContextValidator implements AuthenticationContextValidator {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private String authenticationContextAttribute;

    private String globalFailureMode;

    private String mfaTrustedAuthnAttributeName;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    public String getAuthenticationContextAttribute() {
        return this.authenticationContextAttribute;
    }

    public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
        this.authenticationContextAttribute = authenticationContextAttribute;
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
        final Map<String, Object> attrs = authentication.getAttributes();
        final Object ctxAttr = attrs.get(this.authenticationContextAttribute);
        final Collection<Object> contexts = CollectionUtils.convertValueToCollection(ctxAttr);
        logger.debug("Attempting to match requested authentication context {} against {}", requestedContext, contexts);

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                getAllMultifactorAuthenticationProvidersFromApplicationContext();
        if (providerMap == null) {
            logger.debug("No providers have been configured");
            return Pair.of(Boolean.FALSE, Optional.empty());
        }
        final Optional<MultifactorAuthenticationProvider> requestedProvider =
                locateRequestedProvider(providerMap.values(), requestedContext);

        if (!requestedProvider.isPresent()) {
            logger.debug("Requested authentication provider cannot be recognized.");
            return Pair.of(Boolean.FALSE, Optional.empty());
        }

        if (contexts.stream().filter(ctx -> ctx.toString().equals(requestedContext)).count() > 0) {
            logger.debug("Requested authentication context {} is satisfied", requestedContext);
            return Pair.of(Boolean.TRUE, requestedProvider);
        }


        if (StringUtils.isNotBlank(this.mfaTrustedAuthnAttributeName)
                && attrs.containsKey(this.mfaTrustedAuthnAttributeName)) {
            logger.debug("Requested authentication context {} is satisfied since device is already trusted");
            return Pair.of(Boolean.TRUE, requestedProvider);
        }

        if (attrs.containsKey(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA)
                && attrs.containsKey(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER)) {

            final boolean isBypass = Boolean.class.cast(attrs.get(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA));
            final String bypassedId = attrs.get(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER).toString();

            logger.debug("Found multifactor authentication bypass attributes for provider {}", bypassedId);

            if (isBypass && StringUtils.equals(bypassedId, requestedContext)) {
                logger.debug("Requested authentication context {} is satisfied given mfa was bypass for the authentication attempt");
                return Pair.of(Boolean.TRUE, requestedProvider);
            }

            logger.debug("Either multifactor authentication was not bypassed or the requested context {} does not match the bypassed provider",
                    requestedProvider, bypassedId);
        }

        final Collection<MultifactorAuthenticationProvider> satisfiedProviders =
                getSatisfiedAuthenticationProviders(authentication, providerMap.values());

        if (satisfiedProviders == null) {
            logger.warn("No satisfied multifactor authentication providers are recorded in the current authentication context.");
            return Pair.of(Boolean.FALSE, requestedProvider);
        }

        if (!satisfiedProviders.isEmpty()) {
            final MultifactorAuthenticationProvider[] providers = satisfiedProviders.toArray(new MultifactorAuthenticationProvider[]{});
            OrderComparator.sortIfNecessary(providers);
            final Optional<MultifactorAuthenticationProvider> result = Arrays.stream(providers)
                    .filter(provider -> {
                        final MultifactorAuthenticationProvider p = requestedProvider.get();
                        return provider.equals(p) || provider.getOrder() >= p.getOrder();
                    })
                    .findFirst();

            if (result.isPresent()) {
                logger.debug("Current provider {} already satisfies the authentication requirements of {}; proceed with flow normally.",
                        result.get(), requestedProvider);
                return Pair.of(Boolean.TRUE, requestedProvider);
            }
        }

        logger.debug("No multifactor providers could be located to satisfy the requested context for {}", requestedProvider);

        final RegisteredServiceMultifactorPolicy.FailureModes mode = getMultifactorFailureModeForService(service);
        if (mode == RegisteredServiceMultifactorPolicy.FailureModes.PHANTOM) {
            if (!requestedProvider.get().isAvailable(service)) {
                logger.debug("Service {} is configured to use a {} failure mode for multifactor authentication policy. "
                                + "Since provider {} is unavailable at the moment, CAS will knowingly allow [{}] as a satisfied criteria "
                                + "of the present authentication context", service.getServiceId(),
                        mode, requestedProvider, requestedContext);
                return Pair.of(true, requestedProvider);
            }
        }
        if (mode == RegisteredServiceMultifactorPolicy.FailureModes.OPEN) {
            if (!requestedProvider.get().isAvailable(service)) {
                logger.debug("Service {} is configured to use a {} failure mode for multifactor authentication policy and "
                                + "since provider {} is unavailable at the moment, CAS will consider the authentication satisfied "
                                + "without the presence of {}", service.getServiceId(),
                        mode, requestedProvider, requestedContext);
                return Pair.of(true, satisfiedProviders.stream().findFirst());
            }
        }

        return Pair.of(false, requestedProvider);
    }

    /**
     * Gets all multifactor authentication providers from application context.
     *
     * @return the all multifactor authentication providers from application context
     */
    private Map<String, MultifactorAuthenticationProvider> getAllMultifactorAuthenticationProvidersFromApplicationContext() {
        try {
            return this.applicationContext.getBeansOfType(MultifactorAuthenticationProvider.class, false, true);
        } catch (final Exception e) {
            logger.warn("Could not locate beans of type {} in the application context", MultifactorAuthenticationProvider.class);
        }
        return null;
    }

    private Collection<MultifactorAuthenticationProvider> getSatisfiedAuthenticationProviders(
            final Authentication authentication,
            final Collection<MultifactorAuthenticationProvider> providers) {
        final Collection<Object> contexts = CollectionUtils.convertValueToCollection(
                authentication.getAttributes().get(this.authenticationContextAttribute));

        if (contexts == null || contexts.isEmpty()) {
            logger.debug("No authentication context could be determined based on authentication attribute {}",
                    this.authenticationContextAttribute);
            return null;
        }

        contexts.stream().forEach(context ->
                providers.removeIf(provider -> !provider.getId().equals(context))
        );

        logger.debug("Found {} providers that may satisfy the context", providers.size());
        return providers;
    }


    private static Optional<MultifactorAuthenticationProvider> locateRequestedProvider(
            final Collection<MultifactorAuthenticationProvider> providersArray, final String requestedProvider) {

        return providersArray.stream()
                .filter(provider -> provider.getId().equals(requestedProvider))
                .findFirst();
    }

    private RegisteredServiceMultifactorPolicy.FailureModes getMultifactorFailureModeForService(final RegisteredService service) {
        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy == null || policy.getFailureMode() == null) {
            return RegisteredServiceMultifactorPolicy.FailureModes.valueOf(this.globalFailureMode);
        }
        return policy.getFailureMode();
    }

    public void setMfaTrustedAuthnAttributeName(final String mfaTrustedAuthnAttributeName) {
        this.mfaTrustedAuthnAttributeName = mfaTrustedAuthnAttributeName;
    }

    public void setGlobalFailureMode(final String globalFailureMode) {
        this.globalFailureMode = globalFailureMode;
    }
}
