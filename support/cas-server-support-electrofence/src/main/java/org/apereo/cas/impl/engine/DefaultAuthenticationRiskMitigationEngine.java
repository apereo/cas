package org.apereo.cas.impl.engine;

import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskMitigationEngine;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuthenticationRiskMitigationEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultAuthenticationRiskMitigationEngine implements AuthenticationRiskMitigationEngine {
    @Autowired
    private CasConfigurationProperties casProperties;

    private Set<AuthenticationRiskContingencyPlan> contingencyPlans = new HashSet<>();
    
    @Override
    public Set<AuthenticationRiskContingencyPlan> getContingencyPlans() {
        return this.contingencyPlans;
    }

    @Override
    public void mitigate(final Authentication authentication, final RegisteredService service, 
                         final AuthenticationRiskScore score, final HttpServletRequest request) {
        if (score.getScore() >= casProperties.getAuthn().getAdaptive().getRisk().getThreshold()) {
            final Set<AuthenticationRiskContingencyResponse> responses = 
                    this.contingencyPlans.stream()
                            .map(p -> p.execute(authentication, service, score, request))
                            .collect(Collectors.toSet());
        }
    }
}
