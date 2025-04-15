package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.FinalMultifactorAuthenticationTransactionWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RadiusAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RadiusAuthenticationWebflowEventResolver extends FinalMultifactorAuthenticationTransactionWebflowEventResolver {
    /**
     * Flow scope variable to indicate count of authn attempts.
     */
    public static final String FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS = "totalAuthenticationAttempts";

    private final long allowedAuthenticationAttempts;

    public RadiusAuthenticationWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext,
        final long allowedAuthenticationAttempts) {
        super(webflowEventResolutionConfigurationContext);
        this.allowedAuthenticationAttempts = allowedAuthenticationAttempts;
    }
    
    @Override
    protected Event getAuthenticationFailureErrorEvent(final RequestContext context, final Exception exception) {
        if (allowedAuthenticationAttempts <= 0) {
            return super.getAuthenticationFailureErrorEvent(context, exception);
        }
        val attempts = context.getFlowScope().getLong(FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS, 0L) + 1;
        if (attempts >= allowedAuthenticationAttempts) {
            context.getFlowScope().remove(FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_CANCEL);
        }
        context.getFlowScope().put(FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS, attempts + 1);
        return super.getAuthenticationFailureErrorEvent(context, exception);
    }

}
