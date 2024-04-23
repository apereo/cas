package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TicketAsFirstParameterResourceResolver implements AuditResourceResolver {
    protected final AuthenticationServiceSelectionPlan serviceSelectionStrategy;
    protected final AuditEngineProperties properties;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {

        val jp = AopUtils.unWrapJoinPoint(joinPoint);
        if (jp != null) {
            val arguments = jp.getArgs();
            if (arguments != null) {
                val ticket = arguments[0];
                val auditFormat = AuditTrailManager.AuditFormats.valueOf(properties.getAuditFormat().name());
                if (arguments.length >= 2) {
                    val service = (Service) arguments[1];
                    val values = new HashMap<String, String>();
                    values.put("ticket", ticket.toString());
                    values.put("service", getServiceId(service));
                    return new String[]{auditFormat.serialize(values)};
                }
                return new String[]{auditFormat.serialize(ticket.toString())};
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception object) {
        return resolveFrom(joinPoint, (Object) object);
    }

    private String getServiceId(final Service service) {
        val serviceId = FunctionUtils.doUnchecked(() -> serviceSelectionStrategy.resolveService(service).getId());
        return DigestUtils.abbreviate(serviceId, properties.getAbbreviationLength());
    }
}
