package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import module java.base;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of {@link AuditResourceResolver} that uses the toString version of the return value
 * as the resource.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
@Setter
public class ReturnValueAsStringResourceResolver implements AuditResourceResolver {

    protected AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    protected Function<String[], String[]> resourcePostProcessor = Function.identity();

    @Override
    public @Nullable String[] resolveFrom(final JoinPoint auditableTarget, @Nullable final Object retval) {
        if (retval instanceof final Collection collection) {
            val size = collection.size();
            val returnValues = new String[size];
            var i = 0;
            for (var iter = collection.iterator(); iter.hasNext() && i < size; i++) {
                val o = iter.next();
                if (o != null) {
                    returnValues[i] = toResourceString(o);
                }
            }

            return returnValues;
        }

        if (retval instanceof final Object[] vals) {
            return Arrays.stream(vals).map(this::toResourceString).toArray(String[]::new);
        }

        return new String[]{toResourceString(retval)};
    }

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Exception exception) {
        val message = exception.getMessage();
        if (message != null) {
            return new String[]{toResourceString(message)};
        }
        return new String[]{toResourceString(exception)};
    }

    /**
     * To resource string.
     *
     * @param arg the arg
     * @return the string
     */
    public String toResourceString(@Nullable final Object arg) {
        if (auditFormat == AuditTrailManager.AuditFormats.JSON && arg != null) {
            return postProcess(AuditTrailManager.toJson(arg));
        }
        return arg == null ? StringUtils.EMPTY : postProcess(arg.toString());
    }

    protected String postProcess(final String value) {
        return resourcePostProcessor.apply(new String[]{value})[0];
    }
}
