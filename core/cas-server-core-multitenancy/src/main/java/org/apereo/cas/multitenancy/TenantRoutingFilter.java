package org.apereo.cas.multitenancy;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link TenantRoutingFilter}.
 * Forwards requests whose host name belongs to a tenant to that tenant's paths. The host name of each
 * tenant is worked out once per tenant definition, and worked out again only when the definition is
 * replaced, such as when tenants are reloaded or saved.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class TenantRoutingFilter implements Filter {
    private final TenantExtractor tenantExtractor;

    private final Cache<TenantDefinition, String> tenantHostNames = Caffeine.newBuilder().weakKeys().build();

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        val request = (HttpServletRequest) req;
        val response = (HttpServletResponse) res;
        val servletPath = Strings.CI.prependIfMissing(request.getServletPath(), "/");
        if (isValidServletPath(servletPath)) {
            val tenantDefinitions = tenantExtractor.getTenantsManager().findTenants();
            for (val tenantDefinition : tenantDefinitions) {
                val tenantHostname = tenantHostNames.get(tenantDefinition, TenantRoutingFilter::extractTenantHost);
                if (StringUtils.isNotBlank(tenantHostname) && Strings.CI.equals(request.getServerName(), tenantHostname)) {
                    val dispatch = "/tenants/" + tenantDefinition.getId() + servletPath;
                    LOGGER.info("Routing request [{}] to tenant [{}] at [{}]", request.getRequestURI(), tenantDefinition.getId(), dispatch);
                    val clientInfo = ClientInfoHolder.getClientInfo();
                    if (clientInfo != null) {
                        clientInfo.setTenant(tenantDefinition.getId());
                    }
                    val dispatcher = request.getRequestDispatcher(dispatch);
                    dispatcher.forward(request, response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private static String extractTenantHost(final TenantDefinition tenantDefinition) {
        val bindingContext = tenantDefinition.bindProperties();
        return bindingContext.isBound()
            ? StringUtils.defaultString(extractTenantHost(bindingContext))
            : StringUtils.EMPTY;
    }

    private static @Nullable String extractTenantHost(final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
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
        return !Strings.CI.startsWith(flowId, "/webjars/")
            && !Strings.CI.startsWith(flowId, "/css/")
            && !Strings.CI.startsWith(flowId, "/favicon")
            && !Strings.CI.startsWith(flowId, "/images/")
            && !Strings.CI.startsWith(flowId, "/js/");
    }
}
