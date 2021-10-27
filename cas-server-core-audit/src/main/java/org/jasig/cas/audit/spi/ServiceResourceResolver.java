package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;

import org.jasig.inspektr.audit.spi.AuditResourceResolver;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.util.AopUtils;

/**
 * Extracts the resource as a CAS service for the audit.
 * @author Scott Battaglia
 * @since 3.1.2
 *
 */
public final class ServiceResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        final Service service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        final StringBuilder builder = new StringBuilder(retval.toString());
        builder.append(" for ");
        builder.append(service.getId());

        return new String[] {builder.toString()};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception ex) {
        final Service service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        return new String[] {service.getId()};
    }
}
