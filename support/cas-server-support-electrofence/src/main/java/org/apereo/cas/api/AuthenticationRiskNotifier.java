package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link AuthenticationRiskNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRiskNotifier extends Runnable {

    /**
     * Sets authentication.
     *
     * @param authentication the authentication
     */
    void setAuthentication(Authentication authentication);

    /**
     * Sets registered service.
     *
     * @param service the service
     */
    void setRegisteredService(RegisteredService service);

    /**
     * Sets authentication risk score.
     *
     * @param score the score
     */
    void setAuthenticationRiskScore(AuthenticationRiskScore score);

    /**
     * Notify in the event that an authentication attempt is considered risky.
     */
    void publish();
}
