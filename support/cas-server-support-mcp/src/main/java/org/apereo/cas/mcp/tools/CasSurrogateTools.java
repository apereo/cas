package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
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
     * Find the accounts a person may impersonate.
     *
     * @param username the username
     * @param service  the optional application URL
     * @return the accounts and whether any account may be impersonated
     */
    @McpTool(description = "Find the accounts a person is allowed to impersonate through surrogate authentication, "
        + "optionally for a given application, and whether the person may impersonate any account.",
        title = "Find Surrogate Accounts",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public SurrogateAccounts findSurrogateAccounts(
        @McpToolParam(description = "The username of the person who impersonates other accounts")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login", required = false)
        final @Nullable String service) {
        val surrogateService = surrogateAuthenticationService.getIfAvailable();
        if (surrogateService == null) {
            return new SurrogateAccounts(username, null, false, 0, List.of(), "Surrogate authentication is not configured");
        }
        val webApplicationService = StringUtils.isNotBlank(service)
            ? Optional.ofNullable(serviceFactory.createService(service))
            : Optional.<WebApplicationService>empty();
        val accounts = FunctionUtils.<Collection<String>>doUnchecked(
            () -> surrogateService.getImpersonationAccounts(username, webApplicationService));
        val wildcard = accounts.contains(SurrogateAuthenticationService.WILDCARD_ACCOUNT);
        val named = new TreeSet<String>(accounts);
        named.remove(SurrogateAuthenticationService.WILDCARD_ACCOUNT);
        return new SurrogateAccounts(username, webApplicationService.map(WebApplicationService::getId).orElse(null),
            wildcard, named.size(), List.copyOf(named), null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SurrogateAccounts(String username, @Nullable String service, boolean anyAccount, int count, List<String> accounts,
                                    @Nullable String reason) {
    }
}
