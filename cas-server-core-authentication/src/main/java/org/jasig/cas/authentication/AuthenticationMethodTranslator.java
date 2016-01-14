package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.WebApplicationService;

/**
 * Defines operations that are to translate a received/retrieved authentication method
 * into one that CAS can recognize. This is useful in cases where principal attributes
 * have defined arbitrary names for the MFA trigger, or that service providers are unable
 * to change submitted parameter names in the request to trigger MFA.
 * @author Misagh Moayyed
 * @since 4.3
 */
public interface AuthenticationMethodTranslator {
    /**
     * Translate an authentication method to one that CAS can recognize.
     * Implementations may choose to decide what should happen if no mapping
     * is found between the source and target authentication methods.
     * @param targetService the target service
     * @param receivedAuthenticationMethod the received authentication method
     * @return the translated method name
     */
    String translate(WebApplicationService targetService, String receivedAuthenticationMethod);
}
