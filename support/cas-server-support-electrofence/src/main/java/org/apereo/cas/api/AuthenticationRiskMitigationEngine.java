package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * This is {@link AuthenticationRiskMitigationEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRiskMitigationEngine {

    /**
     * Gets contingency plans.
     *
     * @return the contingency plans
     */
    Set<AuthenticationRiskContingencyPlan> getContingencyPlans();

    /**
     * Mitigate.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param score          the score
     * @param request        the request
     */
    void mitigate(Authentication authentication, 
                  RegisteredService service, 
                  AuthenticationRiskScore score, 
                  HttpServletRequest request);
}
