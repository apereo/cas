package org.apereo.cas.jmx.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This is {@link PrincipalAttributesCacheManagedResource}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@ManagedResource(description = "Service-specific principal attribute cache maintenance")
@RequiredArgsConstructor
public class PrincipalAttributesCacheManagedResource {
    private final ObjectProvider<PrincipalAttributesRepositoryCache> principalAttributesRepositoryCache;

    @ManagedAttribute(description = "Whether a principal attributes repository cache is configured")
    public boolean isAvailable() {
        return principalAttributesRepositoryCache.getIfAvailable() != null;
    }

    /**
     * Invalidate the configured service-specific principal attributes cache.
     */
    @ManagedOperation(description = "Invalidate cached principal attributes for all registered services")
    public void invalidate() {
        val cache = Optional.ofNullable(principalAttributesRepositoryCache.getIfAvailable())
            .orElseThrow(() -> new IllegalStateException("No principal attributes repository cache is configured"));
        cache.invalidate();
    }
}
