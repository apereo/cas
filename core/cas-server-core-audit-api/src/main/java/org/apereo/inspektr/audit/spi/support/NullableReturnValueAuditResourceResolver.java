package org.apereo.inspektr.audit.spi.support;

import module java.base;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.util.DateTimeUtils;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;

/**
 * This is {@link NullableReturnValueAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 1.0
 */
@Setter
@RequiredArgsConstructor
@SuppressWarnings("NullAway")
public class NullableReturnValueAuditResourceResolver implements AuditResourceResolver {
    private static final String MULTIFACTOR_PROVIDER_ATTRIBUTE = "org.apereo.cas.authentication.MultifactorAuthenticationProvider";

    protected Function<String[], @Nullable String[]> resourcePostProcessor = Function.identity();

    private final AuditResourceResolver delegate;

    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    @Override
    public @Nullable String[] resolveFrom(final JoinPoint joinPoint, @Nullable final Object returnValue) {
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
        val resolved = delegate.resolveFrom(joinPoint, returnValue);
        return resolved != null ? resourcePostProcessor.apply(resolved) : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public @Nullable String[] resolveFrom(final JoinPoint joinPoint, final Exception e) {
        val resolved = delegate.resolveFrom(joinPoint, e);
        return resolved != null ? resourcePostProcessor.apply(resolved) : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * An event that routes to a multifactor provider names the provider: its id is the provider's, and it carries the
     * provider among its attributes. Other events, such as success or error, name nothing.
     *
     * @param joinPoint   the join point
     * @param returnValue the returned value
     * @return the id of the multifactor provider, or null
     */
    @Override
    public @Nullable String resolveTarget(final JoinPoint joinPoint, @Nullable final Object returnValue) {
        val event = returnValue instanceof final Collection<?> events && !events.isEmpty()
            ? events.iterator().next()
            : returnValue;
        return event instanceof final Event resolved && resolved.getAttributes() != null
            && resolved.getAttributes().contains(MULTIFACTOR_PROVIDER_ATTRIBUTE)
            ? StringUtils.trimToNull(resolved.getId())
            : null;
    }
}
