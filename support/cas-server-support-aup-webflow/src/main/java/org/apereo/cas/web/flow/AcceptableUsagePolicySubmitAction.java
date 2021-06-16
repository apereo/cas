package org.apereo.cas.web.flow;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @return success if policy acceptance is recorded successfully.
     */
    private Event submit(final RequestContext context) {
        LOGGER.trace("Submitting acceptable usage policy");
        if (repository.submit(context)) {
            return new EventFactorySupport().event(this,
                CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED);
        }
        return error();
    }

    @Audit(action = AuditableActions.AUP_SUBMIT,
        actionResolverName = AuditActionResolvers.AUP_SUBMIT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUP_SUBMIT_RESOURCE_RESOLVER)
    @Override
    public Event doExecute(final RequestContext requestContext) {
        return submit(requestContext);
    }
}
