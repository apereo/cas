package org.apereo.cas.web.flow;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AcceptableUsagePolicyRenderAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class AcceptableUsagePolicyRenderAction extends AbstractAction {
    private final AcceptableUsagePolicyRepository repository;

    @Override
    protected Event doExecute(final RequestContext context) {
        val credential = WebUtils.getCredential(context);
        repository.fetchPolicy(context, credential)
            .ifPresent(policy -> WebUtils.putAcceptableUsagePolicyTermsIntoFlowScope(context, policy));
        return null;
    }
}
