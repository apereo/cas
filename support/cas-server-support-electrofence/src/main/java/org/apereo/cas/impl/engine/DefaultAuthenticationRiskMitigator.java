package org.apereo.cas.impl.engine;

import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import org.apereo.inspektr.audit.annotation.Audit;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultAuthenticationRiskMitigator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public record DefaultAuthenticationRiskMitigator(AuthenticationRiskContingencyPlan contingencyPlan) implements AuthenticationRiskMitigator {
    @Audit(action = AuditableActions.MITIGATE_RISKY_AUTHENTICATION,
        actionResolverName = AuditActionResolvers.ADAPTIVE_RISKY_AUTHENTICATION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.ADAPTIVE_RISKY_AUTHENTICATION_RESOURCE_RESOLVER)
    @Override
    public AuthenticationRiskContingencyResponse mitigate(final Authentication authentication, final RegisteredService service,
                                                          final AuthenticationRiskScore score, final HttpServletRequest request) {
        return this.contingencyPlan.execute(authentication, service, score, request);
    }
}
