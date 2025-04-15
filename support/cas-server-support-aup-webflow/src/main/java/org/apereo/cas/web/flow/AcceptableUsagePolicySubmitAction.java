package org.apereo.cas.web.flow;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.inspektr.audit.annotation.Audit;
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
public class AcceptableUsagePolicySubmitAction extends BaseCasWebflowAction {
    private final AcceptableUsagePolicyRepository repository;


    @Audit(action = AuditableActions.AUP_SUBMIT,
        actionResolverName = AuditActionResolvers.AUP_SUBMIT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUP_SUBMIT_RESOURCE_RESOLVER)
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        return FunctionUtils.doUnchecked(() -> {
            LOGGER.trace("Submitting acceptable usage policy");
            if (repository.submit(requestContext)) {
                return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED);
            }
            return error();
        });
    }
}
