package org.apereo.cas.services;

import org.apereo.cas.authentication.AuthenticationException;

import org.springframework.core.Ordered;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProvider}
 * that describes an external authentication entity/provider
 * matched against a registered service. Providers may be given
 * the ability to check authentication provider for availability
 * before actually producing a relevant identifier.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationProvider extends Serializable, Ordered {

    /**
     * Ensure the provider is available.
     *
     * @return true /false flag once verification is successful.
     * @throws AuthenticationException the authentication exception
     */
    boolean isAvailable() throws AuthenticationException;

    /**
     * Returns the configured bypass provider for this MFA provider.
     *
     * @return - the bypass evaluator
     */
    MultifactorAuthenticationProviderBypass getBypass();

    /**
     * Gets id for this provider.
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the friendly-name for this provider.
     *
     * @return the name
     */
    String getFriendlyName();

    /**
     * Does provider match/support this identifier?
     * The identifier passed may be formed as a regular expression.
     *
     * @param identifier the identifier
     * @return the boolean
     */
    boolean matches(String identifier);

    /**
     * This method will return the failure mode for the provider.
     *
     * @return the FailureMode
     */
    RegisteredServiceMultifactorPolicy.FailureModes failureMode();

}
