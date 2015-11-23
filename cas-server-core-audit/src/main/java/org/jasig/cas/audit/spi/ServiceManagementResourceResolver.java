package org.jasig.cas.audit.spi;

import org.jasig.inspektr.audit.spi.AuditResourceResolver;

import org.aspectj.lang.JoinPoint;
import org.jasig.cas.util.AopUtils;


/**
 * Resolves a service id to the service.
 * <p>
 * The expectation is that args[0] is a Long.
 *
 * @author Scott Battaglia
 * @since 3.4.6
 */
public final class ServiceManagementResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint target, final Object returnValue) {
        return findService(target);
    }

    @Override
    public String[] resolveFrom(final JoinPoint target, final Exception exception) {
        return findService(target);
    }

    /**
     * Find service.
     *
     * @param joinPoint the join point
     * @return the string[]
     */
    private String[] findService(final JoinPoint joinPoint) {
        final JoinPoint j = AopUtils.unWrapJoinPoint(joinPoint);

        final Long id = (Long) j.getArgs()[0];

        if (id == null) {
            return new String[] {""};
        }

        return new String[] {"id=" + id};
    }
}
