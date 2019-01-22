package org.apereo.cas.authentication.bypass;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ChainingMultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface ChainingMultifactorAuthenticationProviderBypass extends MultifactorAuthenticationProviderBypass {

    /**
     * Gets multifactor authentication provider bypass evaluators.
     *
     * @return the multifactor authentication provider bypass evaluators
     */
    List<MultifactorAuthenticationProviderBypass> getMultifactorAuthenticationProviderBypassEvaluators();

    /**
     * Add bypass provider.
     *
     * @param bypass - the bypass provider
     */
    void addMultifactorAuthenticationProviderBypass(MultifactorAuthenticationProviderBypass bypass);

    /**
     * Add multifactor authentication provider bypass.
     *
     * @param bypasses the bypasses
     */
    default void addMultifactorAuthenticationProviderBypass(MultifactorAuthenticationProviderBypass... bypasses) {
        Arrays.stream(bypasses).forEach(this::addMultifactorAuthenticationProviderBypass);
    }

    /**
     * Find multifactor authentication provider bypass.
     *
     * @param providerId the provider id
     * @return the bypass
     */
    MultifactorAuthenticationProviderBypass filterMultifactorAuthenticationProviderBypassBy(String providerId);
}
