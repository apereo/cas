package org.apereo.cas.web.flow;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action to receive and record the AUP response.
 *
 * @author Misagh Moayyed
 * @since 6.1
 */
@RequiredArgsConstructor
public class AcceptableUsagePolicyVerifyServiceAction extends AbstractAction {
    private final AcceptableUsagePolicyRepository repository;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    /**
     * Verify whether the policy is accepted.
     *
     * @param context        the context
     * @param credential     the credential
     * @param messageContext the message context
     * @return success if policy is accepted. {@link CasWebflowConstants#TRANSITION_ID_AUP_MUST_ACCEPT} otherwise.
     */
    private Event verify(final RequestContext context, final Credential credential,
                         final MessageContext messageContext) {

        val registeredService = WebUtils.getRegisteredService(context);
        val authentication = WebUtils.getAuthentication(context);
        val service = WebUtils.getService(context);
        val eventFactorySupport = new EventFactorySupport();

        if (registeredService != null) {
            val audit = AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();

            val property = RegisteredServiceProperty.RegisteredServiceProperties.ACCEPTABLE_USAGE_POLICY_ENABLED;
            if (property.isAssignedTo(registeredService)) {
                val aupEnabled = property.getPropertyBooleanValue(registeredService);
                if (aupEnabled && !repository.verify(context, credential).isAccepted()) {
                    return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT);
                }
            }
        }
        return null;
    }

    @Audit(action = "AUP_VERIFY",
        actionResolverName = "AUP_VERIFY_ACTION_RESOLVER",
        resourceResolverName = "AUP_VERIFY_RESOURCE_RESOLVER")
    @Override
    public Event doExecute(final RequestContext requestContext) {
        return verify(requestContext, WebUtils.getCredential(requestContext), requestContext.getMessageContext());
    }
}
