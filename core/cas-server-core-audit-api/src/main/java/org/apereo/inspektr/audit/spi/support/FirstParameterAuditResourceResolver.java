package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import lombok.Setter;
import org.apereo.cas.util.AopUtils;
import org.aspectj.lang.JoinPoint;
import java.util.List;
import java.util.function.Function;

/**
 * Converts the first argument object into a String resource identifier.
 * If the resource string is set, it will return the argument values into a list,
 * prefixed by the string. otherwise simply returns the argument value as a string.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 1.0
 */
@Setter
public class FirstParameterAuditResourceResolver implements AuditResourceResolver {

    protected Function<String[], String[]> resourcePostProcessor = Function.identity();

    private String resourceString;

    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return toResources(getArguments(joinPoint));
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return toResources(getArguments(joinPoint));
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
        return resourceString != null
            ? this.resourceString + List.of(arg)
            : arg.toString();
    }

    private static Object[] getArguments(final JoinPoint joinPoint) {
        return AopUtils.unWrapJoinPoint(joinPoint).getArgs();
    }

    /**
     * Turn the arguments into a list.
     *
     * @param args the args
     * @return the string[]
     */
    protected String[] toResources(final Object[] args) {
        return this.resourcePostProcessor.apply(new String[]{toResourceString(args[0])});
    }
}
