package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.jee.context.JEEContext;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasDelegatedAuthenticationTools}, available when delegated authentication to external identity providers
 * is included in the build. Client secrets, keys and other provider settings are never returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasDelegatedAuthenticationTools {
    private final CasMcpEvaluationContextFactory evaluationContextFactory;

    private final ObjectProvider<DelegatedIdentityProviders> delegatedIdentityProviders;

    /**
     * List the external identity providers CAS can delegate authentication to.
     *
     * @return the identity providers
     */
    @McpTool(description = "List the external identity providers CAS can delegate authentication to, such as SAML2, "
        + "OpenID Connect, OAuth or CAS servers: the name, type, display name, callback URL, auto-redirect setting and the "
        + "attribute used as the principal id. Client secrets, keys and other provider settings are never returned.",
        title = "List Identity Providers",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> listIdentityProviders() {
        val result = new LinkedHashMap<String, Object>();
        val providers = delegatedIdentityProviders.getIfAvailable();
        if (providers == null) {
            result.put("reason", "Delegated authentication is not configured");
            return result;
        }
        val exchange = evaluationContextFactory.currentHttpExchange();
        val webContext = new JEEContext(exchange.request(), exchange.response());
        val clients = providers.findAllClients(webContext)
            .stream()
            .sorted(Comparator.comparing(Client::getName, String.CASE_INSENSITIVE_ORDER))
            .map(CasDelegatedAuthenticationTools::summarize)
            .toList();
        result.put("count", clients.size());
        result.put("identityProviders", clients);
        return result;
    }

    private static Map<String, Object> summarize(final Client client) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("name", client.getName());
        summary.put("type", AopUtils.getTargetClass(client).getSimpleName());
        if (client instanceof final IndirectClient indirectClient) {
            summary.put("callbackUrl", indirectClient.getCallbackUrl());
        }
        if (client instanceof final BaseClient baseClient) {
            val properties = baseClient.getCustomProperties();
            summary.put("displayName", properties.get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME));
            summary.put("autoRedirectType", Objects.toString(
                properties.get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE), null));
            summary.put("principalAttributeId", properties.get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID));
        }
        return summary;
    }
}
