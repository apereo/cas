package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link AuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRequestRiskCalculator {
    /**
     * Highest risk score for an authn request.
     */
    long HIGHEST_RISK_SCORE = 1;

    /**
     * Lowest risk score for an authn request.
     */
    long LOWEST_RISK_SCORE = 0;

    /**
     * Calculate authentication risk score.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param request        the request
     * @return the authentication risk score
     */
    AuthenticationRiskScore calculate(Authentication authentication,
                                      RegisteredService service,
                                      HttpServletRequest request);
}
