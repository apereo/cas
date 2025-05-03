package org.apereo.cas.multitenancy;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * This is {@link TenantRoutingFilter}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class TenantRoutingFilter implements Filter {
    private final TenantExtractor tenantExtractor;

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        val request = (HttpServletRequest) req;
        val response = (HttpServletResponse) res;
        val servletPath = StringUtils.prependIfMissing(request.getServletPath(), "/");
        if (isValidServletPath(servletPath)) {
            val tenantDefinitions = tenantExtractor.getTenantsManager().findTenants();
            for (val tenantDefinition : tenantDefinitions) {
                val bindingContext = tenantDefinition.bindProperties();
                if (bindingContext.isBound()) {
                    val tenantHostname = extractTenantHost(bindingContext);
                    if (StringUtils.equalsIgnoreCase(request.getServerName(), tenantHostname)) {
                        val dispatch = "/tenants/" + tenantDefinition.getId() + servletPath;
                        LOGGER.info("Routing request [{}] to tenant [{}] at [{}]", request.getRequestURI(), tenantDefinition.getId(), dispatch);
                        val dispatcher = request.getRequestDispatcher(dispatch);
                        dispatcher.forward(request, response);
                        return;
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    private static String extractTenantHost(final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        if (bindingContext.containsBindingFor("cas.server.name")) {
            return FunctionUtils.doUnchecked(() -> {
                val serverName = bindingContext.value().getServer().getName();
                return new URI(serverName).getHost();
            });
        }
        if (bindingContext.containsBindingFor("cas.host.name")) {
            return bindingContext.value().getHost().getName();
        }
        return null;
    }

    private static boolean isValidServletPath(final String flowId) {
        return !StringUtils.startsWithIgnoreCase(flowId, "/webjars/")
            && !StringUtils.startsWithIgnoreCase(flowId, "/css/")
            && !StringUtils.startsWithIgnoreCase(flowId, "/favicon")
            && !StringUtils.startsWithIgnoreCase(flowId, "/images/")
            && !StringUtils.startsWithIgnoreCase(flowId, "/js/");
    }
}
