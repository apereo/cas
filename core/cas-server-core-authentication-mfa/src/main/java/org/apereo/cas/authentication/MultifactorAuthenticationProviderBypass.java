package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
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
     * @return false is request isn't supported and can be bypassed. true otherwise.
     */
    boolean shouldMultifactorAuthenticationProviderExecute(Authentication authentication, RegisteredService registeredService,
                                                           MultifactorAuthenticationProvider provider);
}
