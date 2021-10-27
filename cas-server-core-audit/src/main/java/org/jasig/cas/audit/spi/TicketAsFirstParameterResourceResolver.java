package org.jasig.cas.audit.spi;

import org.jasig.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jasig.cas.util.AopUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from the first parameter of the method call.

 * @author Scott Battaglia
 * @since 3.1.2
 *
 */
@Component("ticketResourceResolver")
public final class TicketAsFirstParameterResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return new String[] {AopUtils.unWrapJoinPoint(joinPoint).getArgs()[0].toString()};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {
        return new String[] {AopUtils.unWrapJoinPoint(joinPoint).getArgs()[0].toString()};
    }
}
