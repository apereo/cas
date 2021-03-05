package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.util.AopUtils;
import org.apereo.cas.validation.Assertion;

import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from
 * the first parameter of the method call as well as the returned value, typically assertion.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Setter
public class TicketValidationResourceResolver extends TicketAsFirstParameterResourceResolver {
    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {
        val results = new LinkedHashMap<String, Object>();

        val args = AopUtils.unWrapJoinPoint(joinPoint).getArgs();
        if (args != null && args.length > 0) {
            val ticketId = args[0].toString();
            results.put("ticket", ticketId);
        }

        if (object instanceof Assertion) {
            val assertion = Assertion.class.cast(object);
            val authn = assertion.getPrimaryAuthentication();
            results.put("principal", authn.getPrincipal().getId());
            val attributes = new HashMap<String, Object>(authn.getAttributes());
            attributes.putAll(authn.getPrincipal().getAttributes());
            results.put("attributes", attributes);
        }

        return results.isEmpty()
            ? ArrayUtils.EMPTY_STRING_ARRAY
            : new String[]{auditFormat.serialize(results)};
    }
}
