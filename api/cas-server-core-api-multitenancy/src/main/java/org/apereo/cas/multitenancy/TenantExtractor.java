package org.apereo.cas.multitenancy;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link TenantExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface TenantExtractor {
    /**
     * Flow url handler.
     */
    FlowUrlHandler FLOW_URL_HANDLER = new DefaultFlowUrlHandler();
    
    /**
     * Tenant pattern definition.
     */
    Pattern PATTERN_TENANTS = Pattern.compile("/?tenants/([^/]+)(?:/.*)?", Pattern.CASE_INSENSITIVE);

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
     * Gets application context.
     *
     * @return the application context
     */
    ApplicationContext getApplicationContext();
    
    /**
     * Extract tenant id from request.
     *
     * @param request the request
     * @return the tenant id
     */
    default Optional<TenantDefinition> extract(final HttpServletRequest request) {
        val flowId = request != null ? FLOW_URL_HANDLER.getFlowId(request) : StringUtils.EMPTY;
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

    /**
     * Tenant id from path.
     *
     * @param requestPath the request path
     * @return the string
     */
    static String tenantIdFromPath(final String requestPath) {
        if (StringUtils.isBlank(requestPath)) {
            return null;
        }
        val matcher = PATTERN_TENANTS.matcher(requestPath);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Get tenant key string.
     *
     * @param tenantDefinition the tenant definition
     * @return the string
     */
    String getTenantKey(TenantDefinition tenantDefinition);
}
