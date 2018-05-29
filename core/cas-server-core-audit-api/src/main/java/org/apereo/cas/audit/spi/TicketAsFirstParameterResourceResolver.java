package org.apereo.cas.audit.spi;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.AopUtils;
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
        final var jp = AopUtils.unWrapJoinPoint(joinPoint);
        if (jp != null && jp.getArgs() != null) {
            return new String[]{jp.getArgs()[0].toString()};
        }
        return new String[]{};
    }
}
