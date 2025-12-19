package org.apereo.cas.api;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link AuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface AuthenticationRequestRiskCalculator {
    /**
     * Calculate authentication risk score.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param clientInfo     the client info
     * @return the authentication risk score
     */
    AuthenticationRiskScore calculate(Authentication authentication,
                                      RegisteredService service,
                                      ClientInfo clientInfo);
}
