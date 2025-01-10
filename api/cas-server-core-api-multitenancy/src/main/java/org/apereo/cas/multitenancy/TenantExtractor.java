package org.apereo.cas.multitenancy;

import lombok.val;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link TenantExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface TenantExtractor {
    /**
     * Tenant extractor bean name.
     */
    String BEAN_NAME = "tenantExtractor";

    /**
     * Gets tenants manager.
     *
     * @return the tenants manager
     */
    TenantsManager getTenantsManager();

    /**
     * Extract tenant id from request.
     *
     * @param request the request
     * @return the tenant id
     */
    default Optional<TenantDefinition> extract(final HttpServletRequest request) {
        val flowId = new DefaultFlowUrlHandler().getFlowId(request);
        return extract(flowId);
    }

    /**
     * Extract tenant id from request context.
     *
     * @param requestContext the request context
     * @return the tenant id
     */
    default Optional<TenantDefinition> extract(final RequestContext requestContext) {
        val request = (HttpServletRequest) requestContext.getExternalContext().getNativeRequest();
        return extract(request);
    }

    /**
     * Extract tenant.
     *
     * @param requestPath the request path
     * @return the optional
     */
    Optional<TenantDefinition> extract(String requestPath);
}
