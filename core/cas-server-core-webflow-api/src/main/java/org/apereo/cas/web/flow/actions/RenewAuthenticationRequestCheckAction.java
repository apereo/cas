package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RenewAuthenticationRequestCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class RenewAuthenticationRequestCheckAction extends AbstractAction {
    private final SingleSignOnParticipationStrategy renewalStrategy;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val ssoParticipation = this.renewalStrategy.supports(requestContext) && this.renewalStrategy.isParticipating(requestContext);
        if (ssoParticipation) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_RENEW);
    }
}
