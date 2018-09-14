package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationProviderBypass extends Serializable {

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
    boolean shouldMultifactorAuthenticationProviderExecute(Authentication authentication, RegisteredService registeredService,
                                                           MultifactorAuthenticationProvider provider,
                                                           HttpServletRequest request);

    /**
     * Method will remove any previous bypass set in the authentication.
     *
     * @param authentication - the authentication
     */
    default void updateAuthenticationToForgetBypass(Authentication authentication) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.FALSE);
    }

    /**
     * Method will set the bypass into the authentication.
     *
     * @param authentication - the authentication
     * @param provider - the provider
     */
    default void updateAuthenticationToRememberBypass(Authentication authentication,
                                                      MultifactorAuthenticationProvider provider) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.TRUE);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, provider.getId());
    }
}
