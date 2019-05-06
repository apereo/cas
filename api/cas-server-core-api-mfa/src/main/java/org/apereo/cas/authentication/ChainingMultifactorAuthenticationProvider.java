package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface ChainingMultifactorAuthenticationProvider extends MultifactorAuthenticationProvider {

    /**
     * The identifier for the chained provider.
     */
    String DEFAULT_IDENTIFIER = "mfa-composite";

    /**
     * Gets multifactor authentication providers.
     *
     * @return the multifactor authentication providers
     */
    Collection<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviders();

    /**
     * Add multifactor authentication provider multifactor authentication provider.
     *
     * @param provider the provider
     * @return the multifactor authentication provider
     */
    MultifactorAuthenticationProvider addMultifactorAuthenticationProvider(MultifactorAuthenticationProvider provider);

    /**
     * Add multifactor authentication providers.
     *
     * @param providers the providers
     */
    void addMultifactorAuthenticationProviders(Collection<MultifactorAuthenticationProvider> providers);

    /**
     * Add multifactor authentication providers.
     *
     * @param providers the providers
     */
    default void addMultifactorAuthenticationProviders(final MultifactorAuthenticationProvider... providers) {
        addMultifactorAuthenticationProviders(Arrays.stream(providers).collect(Collectors.toList()));
    }

    @Override
    default boolean isAvailable(final RegisteredService service) throws AuthenticationException {
        return getMultifactorAuthenticationProviders()
            .stream()
            .allMatch(p -> p.isAvailable(service));
    }

    @Override
    default String getId() {
        return DEFAULT_IDENTIFIER;
    }

    @Override
    default String getFriendlyName() {
        return "Multifactor Provider Selection";
    }

    @Override
    default boolean matches(final String identifier) {
        return getMultifactorAuthenticationProviders()
            .stream()
            .anyMatch(p -> p.matches(identifier));
    }

    @Override
    default RegisteredServiceMultifactorPolicyFailureModes getFailureMode() {
        return RegisteredServiceMultifactorPolicyFailureModes.NONE;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
