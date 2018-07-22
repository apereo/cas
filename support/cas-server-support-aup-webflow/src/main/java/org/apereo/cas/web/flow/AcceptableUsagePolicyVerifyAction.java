package org.apereo.cas.web.flow;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action to receive and record the AUP response.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RequiredArgsConstructor
public class AcceptableUsagePolicyVerifyAction extends AbstractAction {
    private final AcceptableUsagePolicyRepository repository;

    /**
     * Verify whether the policy is accepted.
     *
     * @param context        the context
     * @param credential     the credential
     * @param messageContext the message context
     * @return success if policy is accepted. {@link CasWebflowConstants#TRANSITION_ID_AUP_MUST_ACCEPT} otherwise.
     */
    private Event verify(final RequestContext context, final Credential credential, final MessageContext messageContext) {
        val res = repository.verify(context, credential);
        WebUtils.putPrincipal(context, res.getValue());
        return res.getKey()
            ? new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED)
            : new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return verify(requestContext, WebUtils.getCredential(requestContext), requestContext.getMessageContext());
    }
}
