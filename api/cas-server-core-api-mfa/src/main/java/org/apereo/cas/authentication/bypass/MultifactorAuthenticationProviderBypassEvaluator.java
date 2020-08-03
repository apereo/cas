package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link MultifactorAuthenticationProviderBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationProviderBypassEvaluator extends Serializable, Ordered {

    /**
     * bypass mfa authn attribute.
     */
    String AUTHENTICATION_ATTRIBUTE_BYPASS_MFA = "bypassMultifactorAuthentication";

    /**
     * bypass mfa for provider id authn attribute.
     */
    String AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER = "bypassedMultifactorAuthenticationProviderId";

    /**
     * Eval current bypass rules for the provider.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service in question
     * @param provider          the provider
     * @param request           the request
     * @return false is request isn't supported and can be bypassed. true otherwise.
     */
    boolean shouldMultifactorAuthenticationProviderExecute(Authentication authentication,
                                                           RegisteredService registeredService,
                                                           MultifactorAuthenticationProvider provider,
                                                           HttpServletRequest request);


    /**
     * Indicates whether the authentication attempt carries information
     * that would signal a bypassed attempt.
     *
     * @param authentication   the authentication
     * @param requestedContext the requested context
     * @return true/false
     */
    default boolean isMultifactorAuthenticationBypassed(final Authentication authentication,
                                                        final String requestedContext) {
        return false;
    }

    /**
     * Method will remove any previous bypass set in the authentication.
     *
     * @param authentication - the authentication
     */
    default void forgetBypass(final Authentication authentication) {
    }

    /**
     * Method will set the bypass into the authentication.
     *
     * @param authentication - the authentication
     * @param provider       - the provider
     */
    default void rememberBypass(final Authentication authentication, final MultifactorAuthenticationProvider provider) {
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Gets provider id of this bypass evaluator.
     *
     * @return the id
     */
    String getProviderId();

    /**
     * Gets id.
     *
     * @return the id
     */
    String getId();

    /**
     * Size.
     *
     * @return the int
     */
    default int size() {
        return 1;
    }

    /**
     * Is empty?.
     *
     * @return true/false
     */
    default boolean isEmpty() {
        return false;
    }


    /**
     * Indicate whether this bypass belongs to given multifactor authentication provider.
     *
     * @param providerId the provider id
     * @return the provider if a match, otherwise, empty.
     */
    Optional<MultifactorAuthenticationProviderBypassEvaluator> belongsToMultifactorAuthenticationProvider(String providerId);
}
