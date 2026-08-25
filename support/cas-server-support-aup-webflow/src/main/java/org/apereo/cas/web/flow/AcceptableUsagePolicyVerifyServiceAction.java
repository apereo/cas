package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.AcceptableUsagePolicyStatus;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action to receive and record the AUP response.
 *
 * @author Misagh Moayyed
 * @since 6.1
 */
public class AcceptableUsagePolicyVerifyServiceAction extends BaseAcceptableUsagePolicyAction {

    public AcceptableUsagePolicyVerifyServiceAction(final AcceptableUsagePolicyRepository repository,
                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                    final TenantExtractor tenantExtractor) {
        super(repository, registeredServiceAccessStrategyEnforcer, tenantExtractor);
    }

    @Audit(action = AuditableActions.AUP_VERIFY,
        actionResolverName = AuditActionResolvers.AUP_VERIFY_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUP_VERIFY_RESOURCE_RESOLVER)
    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(requestContext);

        if (registeredService != null) {
            val authentication = WebUtils.getAuthentication(requestContext);
            val service = WebUtils.getService(requestContext);
            val audit = AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .build();
            val accessResult = getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            val aupEnabled = registeredService.getAcceptableUsagePolicy() != null
                && registeredService.getAcceptableUsagePolicy().isEnabled();

            val repository = toAcceptableUsagePolicyRepository(requestContext);
            val res = ObjectUtils.getIfNull(aupEnabled ? repository.verify(requestContext) : null,
                AcceptableUsagePolicyStatus.skipped(authentication.getPrincipal()));
            if (res != null && res.isDenied()) {
                return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT);
            }
        }
        return null;
    }

}
