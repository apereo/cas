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

    /**
     * Register audit resource resolver.
     *
     * @param key      the key
     * @param resolver the resolver
     */
    void registerAuditResourceResolver(String key, AuditResourceResolver resolver);

    /**
     * Register audit action resolver.
     *
     * @param key      the key
     * @param resolver the resolver
     */
    void registerAuditActionResolver(String key, AuditActionResolver resolver);

    /**
     * Register audit action resolvers.
     *
     * @param resolvers the resolvers
     */
    void registerAuditActionResolvers(Map<String, AuditActionResolver> resolvers);

    /**
     * Register audit resource resolvers.
     *
     * @param resolvers the resolvers
     */
    void registerAuditResourceResolvers(Map<String, AuditResourceResolver> resolvers);

    /**
     * Gets audit resource resolvers.
     *
     * @return the audit resource resolvers
     */
    Map<String, AuditResourceResolver> getAuditResourceResolvers();

    /**
     * Gets audit action resolvers.
     *
     * @return the audit action resolvers
     */
    Map<String, AuditActionResolver> getAuditActionResolvers();
}
