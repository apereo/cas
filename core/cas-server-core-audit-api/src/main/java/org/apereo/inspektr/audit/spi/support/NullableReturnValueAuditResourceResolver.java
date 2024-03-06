package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.util.DateTimeUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.execution.Event;
import java.util.HashMap;
import java.util.function.Function;

/**
 * This is {@link NullableReturnValueAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 1.0
 */
@Setter
@RequiredArgsConstructor
public class NullableReturnValueAuditResourceResolver implements AuditResourceResolver {
    protected Function<String[], String[]> resourcePostProcessor = Function.identity();

    private final AuditResourceResolver delegate;

    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object returnValue) {
        if (returnValue == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        if (returnValue instanceof final Event event) {
            val sourceName = event.getSource().getClass().getSimpleName();
            val values = new HashMap<String, String>();
            values.put("event", event.getId());
            values.put("timestamp", DateTimeUtils.localDateTimeOf(event.getTimestamp()).toString());
            values.put("source", sourceName);
            if (event.getAttributes() != null && !event.getAttributes().isEmpty()) {
                val attributes = new HashMap<>(event.getAttributes().asMap());
                attributes.entrySet().removeIf(entry -> entry.getKey().startsWith(CentralAuthenticationService.NAMESPACE));
                attributes.forEach((key, value) -> {
                    if (value != null) {
                        values.put(key, value.toString());
                    }
                });
            }
            if (auditFormat == AuditTrailManager.AuditFormats.JSON) {
                return resourcePostProcessor.apply(new String[]{AuditTrailManager.toJson(values)});
            }
            return resourcePostProcessor.apply(new String[]{values.toString()});
        }
        return resourcePostProcessor.apply(this.delegate.resolveFrom(joinPoint, returnValue));
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception e) {
        return resourcePostProcessor.apply(this.delegate.resolveFrom(joinPoint, e));
    }
}

