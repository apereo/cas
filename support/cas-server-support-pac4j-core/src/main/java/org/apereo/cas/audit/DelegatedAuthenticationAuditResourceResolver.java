package org.apereo.cas.audit;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.pac4j.core.client.Client;

import java.util.HashMap;
import java.util.Objects;

/**
 * This is {@link DelegatedAuthenticationAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DelegatedAuthenticationAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        Objects.requireNonNull(retval, "Return value must not be null");
        val result = AuditableExecutionResult.class.cast(retval);
        val accessCheckOutcome = "Client Access " + BooleanUtils.toString(result.isExecutionFailure(), "Denied", "Granted");

        val values = new HashMap<>();
        values.put("result", accessCheckOutcome);
        if (result.getProperties().containsKey(Client.class.getSimpleName())) {
            values.put("client", result.getProperties().get(Client.class.getSimpleName()));
        }
        result.getRegisteredService().ifPresent(service ->
            values.put("registeredService", service.getName() + ':' + service.getServiceId()));

        return new String[]{auditFormat.serialize(values)};
    }
}
