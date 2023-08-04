package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import lombok.Setter;
import org.aspectj.lang.JoinPoint;
import java.util.function.Function;

/**
 * Abstract AuditResourceResolver for when the resource is the same regardless of an exception or not.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
@Setter
public abstract class AbstractAuditResourceResolver implements AuditResourceResolver {

    protected AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    protected Function<String[], String[]> resourcePostProcessor = Function.identity();

    @Override
    public final String[] resolveFrom(final JoinPoint joinPoint, final Object retVal) {
        return createResource(joinPoint.getArgs());
    }

    @Override
    public final String[] resolveFrom(final JoinPoint joinPoint, final Exception e) {
        return this.resourcePostProcessor.apply(createResource(joinPoint.getArgs()));
    }

    protected abstract String[] createResource(Object[] args);
}
