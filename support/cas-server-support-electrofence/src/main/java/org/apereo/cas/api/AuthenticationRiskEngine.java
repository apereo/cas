package org.apereo.cas.api;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * This is {@link AuthenticationRiskEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRiskEngine {

    /**
     * Gets calculators.
     *
     * @return the calculators
     */
    Set<AuthenticationRequestRiskCalculator> getCalculators();

    /**
     * Calculate score authentication risk score.
     *
     * @param request the request
     * @return the authentication risk score
     */
    AuthenticationRiskScore eval(HttpServletRequest request);
}
