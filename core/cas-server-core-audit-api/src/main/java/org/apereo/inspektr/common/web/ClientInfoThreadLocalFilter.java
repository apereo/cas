package org.apereo.inspektr.common.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import module java.base;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.UnknownTenantException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Creates a {@link ClientInfo} object from the http request
 * and passes it to the {@link ClientInfoHolder} .
 *
 * @author Scott Battaglia
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class ClientInfoThreadLocalFilter implements Filter {
    private final ClientInfoExtractionOptions options;
    private final TenantExtractor tenantExtractor;
    
    /**
     * Record the client info of the request. A request whose tenant header names another tenant
     * than its path is refused with a bad request status.
     *
     * @param request     the request
     * @param response    the response
     * @param filterChain the filter chain
     * @throws IOException      the io exception
     * @throws ServletException the servlet exception
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain)
            throws IOException, ServletException {
        try {
            if (request instanceof final HttpServletRequest httpServletRequest) {
                val tenantId = tenantExtractor.extract(httpServletRequest).map(TenantDefinition::getId).orElse(null);
                val clientInfo = ClientInfo.from(httpServletRequest, options).setTenant(tenantId);
                ClientInfoHolder.setClientInfo(clientInfo);
            }
        } catch (final UnknownTenantException e) {
            LOGGER.debug("Refusing request: [{}]", e.getMessage());
            if (response instanceof final HttpServletResponse httpServletResponse) {
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            return;
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            ClientInfoHolder.clear();
        }
    }
}
