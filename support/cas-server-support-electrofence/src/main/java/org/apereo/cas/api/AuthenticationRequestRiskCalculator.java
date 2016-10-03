package org.apereo.cas.api;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link AuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRequestRiskCalculator {
    /** Highest risk score for an authn request. */
    long HIGHEST_RISK_SCORE = 10;

    /** Lowest risk score for an authn request. */
    long LOWEST_RISK_SCORE = 1;

    /**
     * Calculate authentication risk score.
     *
     * @param request the request
     * @return the authentication risk score
     */
    AuthenticationRiskScore calculate(HttpServletRequest request);
}
