package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.DigestUtils;

import lombok.Setter;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;

/**
 * Extracts the resource as a CAS service for the audit.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@Setter
public class ServiceAuditResourceResolver implements AuditResourceResolver {
    private AuditTrailManager.AuditFormats auditFormat = AuditTrailManager.AuditFormats.DEFAULT;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        val service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        val values = new HashMap<String, String>();
        values.put("return", retval.toString());
        values.put("service", DigestUtils.abbreviate(service.getId()));
        return new String[]{this.auditFormat.serialize(values)};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception ex) {
        val service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        return new String[]{this.auditFormat.serialize(service.getId())};
    }


}
