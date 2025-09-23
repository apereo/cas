package org.apereo.inspektr.common.web;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Creates a {@link ClientInfo} object from the http request
 * and passes it to the {@link ClientInfoHolder} .
 *
 * @author Scott Battaglia
 * @since 1.0
 */
@RequiredArgsConstructor
public class ClientInfoThreadLocalFilter implements Filter {
    private final ClientInfoExtractionOptions options;
    private final TenantExtractor tenantExtractor;
    
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        try {
            if (request instanceof final HttpServletRequest httpServletRequest) {
                val tenantId = tenantExtractor.extract(httpServletRequest).map(TenantDefinition::getId).orElse(null);
                val clientInfo = ClientInfo.from(httpServletRequest, options).setTenant(tenantId);
                ClientInfoHolder.setClientInfo(clientInfo);
            }
            filterChain.doFilter(request, response);
        } finally {
            ClientInfoHolder.clear();
        }
    }
}
