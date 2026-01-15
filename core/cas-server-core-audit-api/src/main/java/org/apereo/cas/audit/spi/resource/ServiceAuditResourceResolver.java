package org.apereo.cas.audit.spi.resource;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * Extracts the resource as a CAS service for the audit.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@RequiredArgsConstructor
public class ServiceAuditResourceResolver implements AuditResourceResolver {
    protected final AuthenticationServiceSelectionPlan serviceSelectionStrategy;
    protected final AuditEngineProperties properties;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, @Nullable final Object retval) {
        val auditFormat = AuditTrailManager.AuditFormats.valueOf(properties.getAuditFormat().name());
        val service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        val values = new HashMap<String, String>();
        values.put("ticketId", Objects.requireNonNull(retval).toString());
        values.put("service", getServiceId(service));
        return new String[]{auditFormat.serialize(values)};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception ex) {
        val auditFormat = AuditTrailManager.AuditFormats.valueOf(properties.getAuditFormat().name());
        val service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        return new String[]{auditFormat.serialize(getServiceId(service))};
    }

    private String getServiceId(final Service givenService) {
        return FunctionUtils.doUnchecked(() -> {
            val service = Objects.requireNonNull(serviceSelectionStrategy.resolveService(givenService));
            return DigestUtils.abbreviate(service.getId(), properties.getAbbreviationLength());
        });
    }
}
