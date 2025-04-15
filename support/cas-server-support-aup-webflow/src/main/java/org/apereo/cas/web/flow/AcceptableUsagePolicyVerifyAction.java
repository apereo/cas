package org.apereo.cas.web.flow;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.AcceptableUsagePolicyStatus;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
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
public class AcceptableUsagePolicyVerifyAction extends BaseCasWebflowAction {
    private final AcceptableUsagePolicyRepository repository;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Audit(action = AuditableActions.AUP_VERIFY,
        actionResolverName = AuditActionResolvers.AUP_VERIFY_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUP_VERIFY_RESOURCE_RESOLVER)
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        return FunctionUtils.doUnchecked(() -> verify(requestContext));
    }

    /**
     * Verify whether the policy is accepted.
     *
     * @param context the context
     * @return {@link CasWebflowConstants#TRANSITION_ID_AUP_ACCEPTED} if policy is
     * accepted. {@link CasWebflowConstants#TRANSITION_ID_AUP_MUST_ACCEPT} otherwise.
     */
    private Event verify(final RequestContext context) throws Throwable {
        val authentication = WebUtils.getAuthentication(context);
        val res = ObjectUtils.defaultIfNull(repository.verify(context),
            AcceptableUsagePolicyStatus.skipped(authentication.getPrincipal()));

        WebUtils.putPrincipal(context, res.getPrincipal());
        WebUtils.putAcceptableUsagePolicyStatusIntoFlowScope(context, res);

        val eventFactorySupport = eventFactory;
        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(context);

        if (registeredService != null) {
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

        return switch (res.getStatus()) {
            case TRUE -> eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED);
            case FALSE -> eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT);
            case UNDEFINED -> eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
        };
    }
}
