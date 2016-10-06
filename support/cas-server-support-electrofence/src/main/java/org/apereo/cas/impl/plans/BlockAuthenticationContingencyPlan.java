package org.apereo.cas.impl.plans;

import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link BlockAuthenticationContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class BlockAuthenticationContingencyPlan extends BaseAuthenticationRiskContingencyPlan {
    public BlockAuthenticationContingencyPlan(final AdaptiveAuthenticationProperties adaptiveProperties) {
        super(adaptiveProperties);
    }

    @Override
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication, final RegisteredService service, 
                                                                    final AuthenticationRiskScore score, final HttpServletRequest request) {
        throw new AuthenticationException("Authentication attempt is blocked");
    }
}
