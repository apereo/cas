package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

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
     * @param authentication the authentication
     * @param service        the service
     * @param request        the request
     * @return the authentication risk score
     */
    AuthenticationRiskScore eval(Authentication authentication, RegisteredService service, HttpServletRequest request);
}
