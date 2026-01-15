package org.apereo.cas.audit.spi.plan;

import module java.base;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.common.spi.PrincipalResolver;

/**
 * This is {@link DefaultAuditTrailRecordResolutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class DefaultAuditTrailRecordResolutionPlan implements AuditTrailRecordResolutionPlan {
    private final Map<String, AuditResourceResolver> auditResourceResolvers = new LinkedHashMap<>();

    private final Map<String, AuditActionResolver> auditActionResolvers = new LinkedHashMap<>();

    private final Map<String, PrincipalResolver> auditPrincipalResolvers = new LinkedHashMap<>();

    @Override
    public void registerAuditResourceResolver(final String key, final AuditResourceResolver resolver) {
        if (BeanSupplier.isNotProxy(resolver)) {
            this.auditResourceResolvers.putIfAbsent(key, resolver);
        }
    }

    @Override
    public void registerAuditPrincipalResolver(final String key, final PrincipalResolver resolver) {
        if (BeanSupplier.isNotProxy(resolver)) {
            this.auditPrincipalResolvers.putIfAbsent(key, resolver);
        }
    }

    @Override
    public void registerAuditActionResolver(final String key, final AuditActionResolver resolver) {
        if (BeanSupplier.isNotProxy(resolver)) {
            this.auditActionResolvers.putIfAbsent(key, resolver);
        }
    }
}
