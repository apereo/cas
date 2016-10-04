package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link BaseAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationRequestRiskCalculator implements AuthenticationRequestRiskCalculator {
    /**
     * CAS event repository instance.
     */
    @Autowired
    @Qualifier("casEventRepository")
    protected CasEventRepository casEventRepository;
    
    @Override
    public final AuthenticationRiskScore calculate(final HttpServletRequest request) {
        return new AuthenticationRiskScore(calculateScore(request));        
    }

    /**
     * Calculate score authentication risk score.
     *
     * @param request the request
     * @return the authentication risk score
     */
    protected long calculateScore(final HttpServletRequest request) {
        return HIGHEST_RISK_SCORE;
    }
}
