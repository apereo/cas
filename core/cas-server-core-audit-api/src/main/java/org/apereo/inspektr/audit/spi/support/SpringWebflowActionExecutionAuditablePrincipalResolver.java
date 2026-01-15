package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.common.spi.BaseJoinPointArgumentAuditPrincipalIdProvider;
import module java.base;
import org.apereo.cas.web.support.WebUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SpringWebflowActionExecutionAuditablePrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 1.0
 */
public class SpringWebflowActionExecutionAuditablePrincipalResolver
    extends BaseJoinPointArgumentAuditPrincipalIdProvider<RequestContext> {
    private final String attributeName;

    public SpringWebflowActionExecutionAuditablePrincipalResolver(final String attributeName) {
        super(0, RequestContext.class);
        this.attributeName = attributeName;
    }

    @Override
    protected String resolveFrom(final RequestContext requestContext, final JoinPoint auditTarget, final Object returnValue) {
        if (requestContext.getFlashScope().contains(attributeName)) {
            return requestContext.getFlashScope().get(attributeName).toString();
        }
        if (requestContext.getRequestScope().contains(attributeName)) {
            return requestContext.getRequestScope().get(attributeName).toString();
        }
        if (requestContext.getFlowScope().contains(attributeName)) {
            return requestContext.getFlowScope().get(attributeName).toString();
        }
        if (requestContext.getConversationScope().contains(attributeName)) {
            return requestContext.getConversationScope().get(attributeName).toString();
        }
        return WebUtils.getRequestParameterOrAttribute(requestContext, attributeName).orElse(UNKNOWN_USER);
    }
}
