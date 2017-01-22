package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link AuthenticationRiskMitigator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRiskMitigator {

    /**
     * Gets contingency plans.
     *
     * @return the contingency plans
     */
    AuthenticationRiskContingencyPlan getContingencyPlan();

    /**
     * Mitigate.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param score          the score
     * @param request        the request
     * @return the responses
     */
    AuthenticationRiskContingencyResponse mitigate(Authentication authentication,
                                                   RegisteredService service,
                                                   AuthenticationRiskScore score,
                                                   HttpServletRequest request);
}
