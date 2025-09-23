package org.apereo.cas.web.flow;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.AcceptableUsagePolicyStatus;
import org.apereo.cas.services.WebBasedRegisteredService;
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
 * @since 6.1
 */
@RequiredArgsConstructor
public class AcceptableUsagePolicyVerifyServiceAction extends BaseCasWebflowAction {
    private final AcceptableUsagePolicyRepository repository;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Audit(action = AuditableActions.AUP_VERIFY,
        actionResolverName = AuditActionResolvers.AUP_VERIFY_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUP_VERIFY_RESOURCE_RESOLVER)
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        return verify(requestContext);
    }

    private Event verify(final RequestContext context) throws Throwable {
        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(context);

        if (registeredService != null) {
            val authentication = WebUtils.getAuthentication(context);
            val service = WebUtils.getService(context);
            val eventFactorySupport = eventFactory;
            val audit = AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();

            val aupEnabled = registeredService.getAcceptableUsagePolicy() != null
                             && registeredService.getAcceptableUsagePolicy().isEnabled();
            val res = ObjectUtils.getIfNull(aupEnabled ? repository.verify(context) : null,
                AcceptableUsagePolicyStatus.skipped(authentication.getPrincipal()));
            if (res.isDenied()) {
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT);
            }
        }
        return null;
    }
}
