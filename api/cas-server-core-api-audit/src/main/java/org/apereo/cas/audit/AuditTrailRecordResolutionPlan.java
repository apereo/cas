package org.apereo.cas.audit;

import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.common.spi.PrincipalResolver;

import java.util.Arrays;
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
     * @param resolver the resolver
     * @param keys     the key
     */
    default void registerAuditResourceResolver(final AuditResourceResolver resolver, final String... keys) {
        Arrays.stream(keys).forEach(k -> registerAuditResourceResolver(k, resolver));
    }

    /**
     * Register audit resource resolver.
     *
     * @param key      the key
     * @param resolver the resolver
     */
    void registerAuditResourceResolver(String key, AuditResourceResolver resolver);

    /**
     * Register audit principal resolver.
     *
     * @param key      the key
     * @param resolver the resolver
     */
    void registerAuditPrincipalResolver(String key, PrincipalResolver resolver);


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
     * @param resolver the resolver
     * @param keys     the keys
     */
    default void registerAuditActionResolvers(final AuditActionResolver resolver, final String... keys) {
        Arrays.stream(keys).forEach(k -> registerAuditActionResolver(k, resolver));
    }
    
    /**
     * Register audit resource resolvers.
     *
     * @param resolver the resolver
     * @param keys     the keys
     */
    default void registerAuditResourceResolvers(final AuditResourceResolver resolver,
                                                final String... keys) {
        Arrays.stream(keys).forEach(k -> registerAuditResourceResolver(k, resolver));
    }

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

    /**
     * Gets audit principal resolvers.
     *
     * @return the audit action resolvers
     */
    Map<String, PrincipalResolver> getAuditPrincipalResolvers();
}
