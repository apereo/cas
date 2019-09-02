package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.DigestUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from the first parameter of the method call.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@Slf4j
public class TicketAsFirstParameterResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception object) {
        return resolveFrom(joinPoint, (Object) object);
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {
        final JoinPoint jp = AopUtils.unWrapJoinPoint(joinPoint);
        if (jp != null) {
            final Object[] arguments = jp.getArgs();
            if (arguments != null) {
                final Object ticket = arguments[0];
                if (arguments.length >= 2) {
                    final Service service = (Service) arguments[1];
                    final String builder = ticket.toString() + " for " + DigestUtils.abbreviate(service.getId());
                    return new String[]{builder};
                }
                return new String[]{ticket.toString()};
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
