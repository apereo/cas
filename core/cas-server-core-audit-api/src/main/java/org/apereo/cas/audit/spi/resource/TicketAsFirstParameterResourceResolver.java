package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.DigestUtils;

import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from the first parameter of the method call.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@Setter
public class TicketAsFirstParameterResourceResolver implements AuditResourceResolver {
    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

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
                    val values = new HashMap<String, String>();
                    values.put("ticket", ticket.toString());
                    values.put("service", DigestUtils.abbreviate(service.getId()));
                    return new String[]{auditFormat.serialize(values)};
                }
                return new String[]{auditFormat.serialize(ticket.toString())};
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
