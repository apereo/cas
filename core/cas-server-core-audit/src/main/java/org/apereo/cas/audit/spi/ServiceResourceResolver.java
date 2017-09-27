package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Extracts the resource as a CAS service for the audit.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
public class ServiceResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        final Service service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        final StringBuilder builder = new StringBuilder(retval.toString())
                .append(" for ")
                .append(DigestUtils.abbreviate(service.getId()));

        return new String[]{builder.toString()};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception ex) {
        final Service service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        return new String[]{service.getId()};
    }
}
