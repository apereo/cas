package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link AuthenticationRiskNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRiskNotifier {

    /**
     * Notify in the event that an authentication attempt is considered risky.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param score          the score
     */
    void notify(Authentication authentication, RegisteredService service, AuthenticationRiskScore score);
}
