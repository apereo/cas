package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.multitenancy.TenantExtractor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action to receive and record the AUP response.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@Getter
public class AcceptableUsagePolicySubmitAction extends BaseAcceptableUsagePolicyAction {

    public AcceptableUsagePolicySubmitAction(final AcceptableUsagePolicyRepository repository,
                                             final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                             final TenantExtractor tenantExtractor) {
        super(repository, registeredServiceAccessStrategyEnforcer, tenantExtractor);
    }

    @Audit(action = AuditableActions.AUP_SUBMIT,
        actionResolverName = AuditActionResolvers.AUP_SUBMIT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUP_SUBMIT_RESOURCE_RESOLVER)
    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val repository = toAcceptableUsagePolicyRepository(requestContext);
        if (repository.submit(requestContext)) {
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED);
        }
        return error();
    }
}
