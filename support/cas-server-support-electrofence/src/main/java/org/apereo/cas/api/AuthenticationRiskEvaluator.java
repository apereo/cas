package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import org.apereo.inspektr.common.web.ClientInfo;
import java.util.List;

/**
 * This is {@link AuthenticationRiskEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationRiskEvaluator {

    /**
     * Gets calculators.
     *
     * @return the calculators
     */
    List<AuthenticationRequestRiskCalculator> getCalculators();

    /**
     * Calculate final authentication risk score.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param clientInfo     the client info
     * @return the authentication risk score
     */
    AuthenticationRiskScore evaluate(Authentication authentication, RegisteredService service, ClientInfo clientInfo);

    /**
     * Is risky authentication risk score?.
     *
     * @param score          the score
     * @param authentication the authentication
     * @param service        the service
     * @return true or false
     */
    boolean isRiskyAuthenticationScore(AuthenticationRiskScore score, Authentication authentication, RegisteredService service);

}
