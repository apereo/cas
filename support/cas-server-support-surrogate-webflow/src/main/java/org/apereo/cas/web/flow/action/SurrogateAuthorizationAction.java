package org.apereo.cas.web.flow.action;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SurrogateAuthorizationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class SurrogateAuthorizationAction extends BaseCasWebflowAction {
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val svc = WebUtils.getRegisteredService(requestContext);
        if (svc != null) {
            val authentication = WebUtils.getAuthentication(requestContext);
            val service = WebUtils.getService(requestContext);
            val audit = AuditableContext.builder().service(service)
                .service(service)
                .authentication(authentication)
                .registeredService(svc)
                .build();
            return FunctionUtils.doUnchecked(() -> {
                val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
                accessResult.throwExceptionIfNeeded();
                return success();
            });
        }
        return null;
    }
}
