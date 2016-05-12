package org.apereo.cas.audit.spi;

import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.apereo.cas.util.AopUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from the first parameter of the method call.

 * @author Scott Battaglia
 * @since 3.1.2
 *
 */
@RefreshScope
@Component("ticketResourceResolver")
public class TicketAsFirstParameterResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return new String[] {AopUtils.unWrapJoinPoint(joinPoint).getArgs()[0].toString()};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {
        return new String[] {AopUtils.unWrapJoinPoint(joinPoint).getArgs()[0].toString()};
    }
}
