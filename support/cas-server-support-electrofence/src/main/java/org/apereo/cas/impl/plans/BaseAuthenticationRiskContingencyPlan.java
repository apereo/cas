package org.apereo.cas.impl.plans;

import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link BaseAuthenticationRiskContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationRiskContingencyPlan implements AuthenticationRiskContingencyPlan {

    /**
     * CAS properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Override
    public AuthenticationRiskContingencyResponse execute(final Authentication authentication,
                                                         final RegisteredService service,
                                                         final AuthenticationRiskScore score,
                                                         final HttpServletRequest request) {
        return null;
    }
}
