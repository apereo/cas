package org.apereo.cas.audit;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.pac4j.core.client.Client;

import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

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

        val builder = new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
            .append("result", accessCheckOutcome);
        if (result.getProperties().containsKey(Client.class.getSimpleName())) {
            builder.append("client", result.getProperties().get(Client.class.getSimpleName()));
        }
        result.getRegisteredService().ifPresent(service ->
            builder.append("registeredService", service.getName() + ':' + service.getServiceId()));

        return new String[]{builder.toString()};
    }
}
