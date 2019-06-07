package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;
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
     * @param service - the service
     * @return true /false flag once verification is successful.
     * @throws AuthenticationException the authentication exception
     */
    boolean isAvailable(RegisteredService service) throws AuthenticationException;

    /**
     * Returns the configured bypass provider for this MFA provider.
     *
     * @return - the bypass evaluator
     */
    MultifactorAuthenticationProviderBypassEvaluator getBypassEvaluator();

    /**
     * Returns the configured failure mode evaluator for this MFA provider.
     *
     * @return the failuremode evaluator
     */
    MultifactorAuthenticationFailureModeEvaluator getFailureModeEvaluator();

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
    RegisteredServiceMultifactorPolicyFailureModes getFailureMode();

    /**
     * Creates a unique mark that identifies this provider instance.
     *
     * @return - the mark
     */
    default String createUniqueId() {
        return getId().concat(String.valueOf(hashCode()));
    }

    /**
     * Validates that the passed mark was created by this provider.
     *
     * @param id - the id to validate
     * @return - true if the mark was created by this provider
     */
    default boolean validateId(final String id) {
        return id != null && createUniqueId().equals(id);
    }

}
