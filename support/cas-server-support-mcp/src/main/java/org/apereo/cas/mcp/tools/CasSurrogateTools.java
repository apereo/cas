package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasSurrogateTools}, available when surrogate authentication (impersonation) is included in the build.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasSurrogateTools {
    private final ObjectProvider<SurrogateAuthenticationService> surrogateAuthenticationService;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    /**
     * Find the accounts a person is allowed to impersonate.
     *
     * @param username the username of the person who impersonates others
     * @param service  the optional application URL the accounts are evaluated for
     * @return the accounts the person may impersonate
     */
    @McpTool(description = "Find the accounts a person is allowed to impersonate through surrogate authentication, "
        + "optionally for a given application, and whether the person may impersonate any account.",
        title = "Find Surrogate Accounts",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findSurrogateAccounts(
        @McpToolParam(description = "The username of the person who impersonates other accounts")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login", required = false)
        final @Nullable String service) {
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        val surrogateService = surrogateAuthenticationService.getIfAvailable();
        if (surrogateService == null) {
            result.put("reason", "Surrogate authentication is not configured");
            return result;
        }
        val webApplicationService = StringUtils.isNotBlank(service)
            ? Optional.ofNullable(serviceFactory.createService(service))
            : Optional.<WebApplicationService>empty();
        webApplicationService.ifPresent(application -> result.put("service", application.getId()));
        val accounts = FunctionUtils.<Collection<String>>doUnchecked(
            () -> surrogateService.getImpersonationAccounts(username, webApplicationService));
        val wildcard = accounts.contains(SurrogateAuthenticationService.WILDCARD_ACCOUNT);
        result.put("anyAccount", wildcard);
        val named = new TreeSet<String>(accounts);
        named.remove(SurrogateAuthenticationService.WILDCARD_ACCOUNT);
        result.put("count", named.size());
        result.put("accounts", named);
        return result;
    }
}
