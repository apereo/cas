package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.util.AopUtils;

import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Audit resource resolver for logout requests.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
public class LogoutRequestResourceResolver implements AuditResourceResolver {

    private static String[] toResources(final Object[] args) {
        val object = args[0];
        val request = (SingleLogoutExecutionRequest) object;
        return new String[]{request.getTicketGrantingTicket().getId()};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }
}
