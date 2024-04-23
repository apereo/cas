package org.apereo.cas.impl.plans;

import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.Event;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link BlockAuthenticationContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class BlockAuthenticationContingencyPlan extends BaseAuthenticationRiskContingencyPlan {
    /**
     * Block authentication event.
     */
    public static final String EVENT_ID_BLOCK_AUTHN = "blockedAuthentication";

    public BlockAuthenticationContingencyPlan(final CasConfigurationProperties casProperties, final ApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    @Override
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication, final RegisteredService service,
                                                                    final AuthenticationRiskScore score, final HttpServletRequest request) {

        return new AuthenticationRiskContingencyResponse(new Event(this, EVENT_ID_BLOCK_AUTHN));
    }
}
