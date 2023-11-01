package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.validation.Assertion;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.aspectj.lang.JoinPoint;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from
 * the first parameter of the method call as well as the returned value, typically assertion.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TicketValidationResourceResolver extends TicketAsFirstParameterResourceResolver {

    public TicketValidationResourceResolver(final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                            final AuditEngineProperties properties) {
        super(serviceSelectionStrategy, properties);
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object returnValue) {
        val results = new HashMap<String, Object>();

        val args = AopUtils.unWrapJoinPoint(joinPoint).getArgs();
        if (args != null && args.length > 0) {
            val ticketId = args[0].toString();
            results.put("ticket", ticketId);
        }

        if (returnValue instanceof final Assertion assertion) {
            val authn = assertion.getPrimaryAuthentication();
            results.put("principal", authn.getPrincipal().getId());
            val attributes = new HashMap<String, Object>(authn.getAttributes());
            attributes.putAll(authn.getPrincipal().getAttributes());
            results.put("attributes", attributes);
        }

        val auditFormat = AuditTrailManager.AuditFormats.valueOf(properties.getAuditFormat().name());
        return results.isEmpty()
            ? ArrayUtils.EMPTY_STRING_ARRAY
            : new String[]{auditFormat.serialize(finalizeResources(results, joinPoint, returnValue))};
    }

    protected Map<String, Object> finalizeResources(final Map<String, Object> results,
                                                    final JoinPoint joinPoint, final Object returnValue) {
        return results;
    }
}
