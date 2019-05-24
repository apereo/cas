package org.apereo.cas.authentication.bypass;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ChainingMultifactorAuthenticationProviderBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface ChainingMultifactorAuthenticationProviderBypassEvaluator extends MultifactorAuthenticationProviderBypassEvaluator {

    /**
     * Gets multifactor authentication provider bypass evaluators.
     *
     * @return the multifactor authentication provider bypass evaluators
     */
    List<MultifactorAuthenticationProviderBypassEvaluator> getMultifactorAuthenticationProviderBypassEvaluators();

    /**
     * Add bypass provider.
     *
     * @param bypass - the bypass provider
     */
    void addMultifactorAuthenticationProviderBypassEvaluator(MultifactorAuthenticationProviderBypassEvaluator bypass);

    /**
     * Add multifactor authentication provider bypass.
     *
     * @param bypasses the bypasses
     */
    default void addMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassEvaluator... bypasses) {
        Arrays.stream(bypasses).forEach(this::addMultifactorAuthenticationProviderBypassEvaluator);
    }

    /**
     * Find multifactor authentication provider bypass.
     *
     * @param providerId the provider id
     * @return the bypass
     */
    MultifactorAuthenticationProviderBypassEvaluator filterMultifactorAuthenticationProviderBypassEvaluatorsBy(String providerId);
}
