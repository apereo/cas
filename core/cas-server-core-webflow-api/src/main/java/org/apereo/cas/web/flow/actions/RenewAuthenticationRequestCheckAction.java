package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RenewAuthenticationRequestCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class RenewAuthenticationRequestCheckAction extends BaseCasWebflowAction {
    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .requestContext(requestContext)
            .build();
        val ssoParticipation = singleSignOnParticipationStrategy.supports(ssoRequest)
            && singleSignOnParticipationStrategy.isParticipating(ssoRequest);
        if (ssoParticipation) {
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
        }
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_RENEW);
    }
}
