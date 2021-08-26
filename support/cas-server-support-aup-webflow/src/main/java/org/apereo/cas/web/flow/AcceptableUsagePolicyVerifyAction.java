package org.apereo.cas.web.flow;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
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
public class AcceptableUsagePolicyVerifyAction extends AbstractAction {
    private final AcceptableUsagePolicyRepository repository;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Audit(action = AuditableActions.AUP_VERIFY,
        actionResolverName = AuditActionResolvers.AUP_VERIFY_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUP_VERIFY_RESOURCE_RESOLVER)
    @Override
    public Event doExecute(final RequestContext requestContext) {
        return verify(requestContext);
    }

    /**
     * Verify whether the policy is accepted.
     *
     * @param context the context
     * @return {@link CasWebflowConstants#TRANSITION_ID_AUP_ACCEPTED} if policy is
     * accepted. {@link CasWebflowConstants#TRANSITION_ID_AUP_MUST_ACCEPT} otherwise.
     */
    private Event verify(final RequestContext context) {

        val res = repository.verify(context);
        WebUtils.putPrincipal(context, res.getPrincipal());
        WebUtils.putAcceptableUsagePolicyStatusIntoFlowScope(context, res);

        val eventFactorySupport = new EventFactorySupport();
        val registeredService = WebUtils.getRegisteredService(context);

        if (registeredService != null) {
            val authentication = WebUtils.getAuthentication(context);
            val service = WebUtils.getService(context);
            val audit = AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();

            val aupEnabled = registeredService.getAcceptableUsagePolicy() != null
                && registeredService.getAcceptableUsagePolicy().isEnabled();
            if (!aupEnabled) {
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED);
            }
        }

        return res.isAccepted()
            ? eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED)
            : eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT);
    }
}
