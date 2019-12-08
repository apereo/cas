package org.apereo.cas.audit.spi.plan;

import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;

import lombok.Getter;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DefaultAuditTrailRecordResolutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class DefaultAuditTrailRecordResolutionPlan implements AuditTrailRecordResolutionPlan {
    private static final int MAP_SIZE = 8;

    private final Map<String, AuditResourceResolver> auditResourceResolvers = new LinkedHashMap<>(MAP_SIZE);
    private final Map<String, AuditActionResolver> auditActionResolvers = new LinkedHashMap<>(MAP_SIZE);

    @Override
    public void registerAuditResourceResolver(final String key, final AuditResourceResolver resolver) {
        this.auditResourceResolvers.put(key, resolver);
    }

    @Override
    public void registerAuditActionResolver(final String key, final AuditActionResolver resolver) {
        this.auditActionResolvers.put(key, resolver);
    }

    @Override
    public void registerAuditActionResolvers(final Map<String, AuditActionResolver> resolvers) {
        this.auditActionResolvers.putAll(resolvers);
    }

    @Override
    public void registerAuditResourceResolvers(final Map<String, AuditResourceResolver> resolvers) {
        this.auditResourceResolvers.putAll(resolvers);
    }
}
