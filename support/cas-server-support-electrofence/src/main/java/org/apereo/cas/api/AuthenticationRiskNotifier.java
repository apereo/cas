package org.apereo.cas.api;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.inspektr.common.web.ClientInfo;

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
     * Sets client info.
     *
     * @param request the request
     */
    void setClientInfo(ClientInfo request);

    /**
     * Notify in the event that an authentication attempt is considered risky.
     *
     * @throws Throwable the throwable
     */
    void publish() throws Throwable;

    /**
     * Build risk token.
     *
     * @return the string
     * @throws Throwable the throwable
     */
    String createRiskToken() throws Throwable;
}
