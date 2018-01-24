package org.apereo.cas.audit;

import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;

import java.util.Map;

/**
 * This is {@link AuditTrailRecordResolutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface AuditTrailRecordResolutionPlan {

    void registerAuditResourceResolver(String key, AuditResourceResolver resolver);

    void registerAuditActionResolver(String key, AuditActionResolver resolver);

    void registerAuditActionResolvers(Map<String, AuditActionResolver> resolvers);

    void registerAuditResourceResolvers(Map<String, AuditResourceResolver> resolvers);
    
    Map<String, AuditResourceResolver> getAuditResourceResolvers();

    Map<String, AuditActionResolver> getAuditActionResolvers();
}
