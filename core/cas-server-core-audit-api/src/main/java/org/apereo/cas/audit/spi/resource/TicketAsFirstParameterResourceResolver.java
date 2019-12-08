package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.DigestUtils;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from the first parameter of the method call.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
public class TicketAsFirstParameterResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception object) {
        return resolveFrom(joinPoint, (Object) object);
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {
        val jp = AopUtils.unWrapJoinPoint(joinPoint);
        if (jp != null) {
            val arguments = jp.getArgs();
            if (arguments != null) {
                val ticket = arguments[0];
                if (arguments.length >= 2) {
                    val service = (Service) arguments[1];
                    val builder = ticket.toString() + " for " + DigestUtils.abbreviate(service.getId());
                    return new String[]{builder};
                }
                return new String[]{ticket.toString()};
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
