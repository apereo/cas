package org.apereo.cas.web.flow;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
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
@Slf4j
@Getter
public class AcceptableUsagePolicySubmitAction extends AbstractAction {
    private final AcceptableUsagePolicyRepository repository;

    /**
     * Record the fact that the policy is accepted.
     *
     * @param context    the context
     * @param credential the credential
     * @return success if policy acceptance is recorded successfully.
     */
    private Event submit(final RequestContext context, final Credential credential) {
        LOGGER.trace("Submitting acceptable usage policy request for [{}]", credential);
        if (repository.submit(context, credential)) {
            return new EventFactorySupport().event(this,
                CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED);
        }
        return error();
    }

    @Audit(action = "AUP_SUBMIT",
        actionResolverName = "AUP_SUBMIT_ACTION_RESOLVER",
        resourceResolverName = "AUP_SUBMIT_RESOURCE_RESOLVER")
    @Override
    public Event doExecute(final RequestContext requestContext) {
        val credential = WebUtils.getCredential(requestContext);
        return submit(requestContext, credential);
    }
}
