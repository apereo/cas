package org.apereo.cas.validation;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.services.UnauthorizedServiceException;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.client.Client;

/**
 * This is {@link RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer extends BaseAuditableExecution {
    @Audit(action = "DELEGATED_CLIENT",
        actionResolverName = "DELEGATED_CLIENT_ACTION_RESOLVER",
        resourceResolverName = "DELEGATED_CLIENT_RESOURCE_RESOLVER")
    @Override
    public AuditableExecutionResult execute(final AuditableContext context) {
        val result = AuditableExecutionResult.of(context);
        if (context.getRegisteredService().isPresent() && context.getProperties().containsKey(Client.class.getSimpleName())) {
            val registeredService = context.getRegisteredService().orElseThrow();
            val clientName = context.getProperties().get(Client.class.getSimpleName()).toString();
            LOGGER.trace("Checking delegated access strategy of [{}] for client [{}]", registeredService, clientName);
            val policy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
            if (policy != null) {
                if (!policy.isProviderAllowed(clientName, registeredService)) {
                    LOGGER.debug("Delegated access strategy for [{}] does not permit client [{}]",
                        registeredService, clientName);
                    val e = new UnauthorizedServiceException(
                        UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
                    result.setException(e);
                }
            }
        }
        return result;
    }
}
