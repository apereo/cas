package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import lombok.Setter;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import java.util.HashMap;
import java.util.function.Function;

/**
 * An {@link AuditResourceResolver} that resolves resource as a target object's toString method call.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0
 */
@Setter
public class ObjectToStringResourceResolver implements AuditResourceResolver {

    protected Function<String[], String[]> resourcePostProcessor = Function.identity();

    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    @Override
    public String[] resolveFrom(final JoinPoint target, final Object returnValue) {
        return resourcePostProcessor.apply(new String[]{toResourceString(target.getTarget())});
    }

    @Override
    public String[] resolveFrom(final JoinPoint target, final Exception exception) {
        val values = new HashMap<>();
        values.put("target", toResourceString(target.getTarget()));
        values.put("exception", toResourceString(exception.getMessage()));
        return resourcePostProcessor.apply(new String[]{toResourceString(values)});
    }

    /**
     * To resource string.
     *
     * @param arg the arg
     * @return the string
     */
    public String toResourceString(final Object arg) {
        if (auditFormat == AuditTrailManager.AuditFormats.JSON) {
            return AuditTrailManager.toJson(arg);
        }
        return arg.toString();
    }
}
