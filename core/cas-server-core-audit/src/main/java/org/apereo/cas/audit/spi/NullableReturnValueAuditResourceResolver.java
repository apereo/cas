package org.apereo.cas.audit.spi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.execution.Event;

import java.util.Date;

/**
 * This is {@link NullableReturnValueAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class NullableReturnValueAuditResourceResolver implements AuditResourceResolver {
    private final AuditResourceResolver delegate;

    public NullableReturnValueAuditResourceResolver(final AuditResourceResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object o) {
        if (o == null) {
            return new String[0];
        }
        if (o instanceof Event) {
            final Event event = Event.class.cast(o);

            final String sourceName = event.getSource().getClass().getSimpleName();
            final String result =
                    new ToStringBuilder(event, ToStringStyle.NO_CLASS_NAME_STYLE)
                            .append("event", event.getId())
                            .append("timestamp", new Date(event.getTimestamp()))
                            .append("source", sourceName)
                            .toString();
            return new String[]{result};
        }
        return this.delegate.resolveFrom(joinPoint, o);
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception e) {
        return this.delegate.resolveFrom(joinPoint, e);
    }
}
