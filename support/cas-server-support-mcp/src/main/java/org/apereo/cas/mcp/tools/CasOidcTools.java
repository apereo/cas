package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwk.JsonWebKey;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasOidcTools}, available when OpenID Connect support is included in the build.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasOidcTools {
    private final ServicesManager servicesManager;

    private final ObjectProvider<OidcIssuerService> oidcIssuerService;

    private final ObjectProvider<OidcJsonWebKeystoreGeneratorService> oidcJsonWebKeystoreGeneratorService;

    /**
     * Find an OAuth or OpenID Connect client registration by its client id.
     *
     * @param clientId the client id
     * @return the client registration summary
     */
    @McpTool(description = "Find an OAuth or OpenID Connect client registration by its client id and summarize it: "
        + "redirect URI pattern, grant and response types, scopes, client authentication method, token settings and, "
        + "for OpenID Connect clients, the issuer and ID token signing and encryption settings. Client secrets are never "
        + "returned, only whether one is configured.",
        title = "Find OpenID Connect Client",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findOidcClient(
        @McpToolParam(description = "The client id of the OAuth or OpenID Connect client")
        final String clientId) {
        val result = new LinkedHashMap<String, Object>();
        result.put("clientId", clientId);
        val client = servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .filter(OAuthRegisteredService.class::isInstance)
            .map(OAuthRegisteredService.class::cast)
            .filter(service -> clientId.equals(service.getClientId()))
            .findFirst()
            .orElse(null);
        if (client == null) {
            result.put("reason", "No OAuth or OpenID Connect client is registered with client id " + clientId);
            return result;
        }
        result.put("id", client.getId());
        result.put("name", client.getName());
        result.put("type", client.getFriendlyName());
        result.put("redirectUriPattern", client.getServiceId());
        result.put("enabled", client.getAccessStrategy().isServiceAccessAllowed(client, null));
        result.put("supportedGrantTypes", new TreeSet<>(client.getSupportedGrantTypes()));
        result.put("supportedResponseTypes", new TreeSet<>(client.getSupportedResponseTypes()));
        result.put("scopes", new TreeSet<>(client.getScopes()));
        result.put("tokenEndpointAuthenticationMethod", client.getTokenEndpointAuthenticationMethod());
        result.put("clientSecretConfigured", client.getClientSecrets() != null && !client.getClientSecrets().isEmpty());
        result.put("bypassApprovalPrompt", client.isBypassApprovalPrompt());
        result.put("generateRefreshToken", client.isGenerateRefreshToken());
        result.put("jwtAccessToken", client.isJwtAccessToken());
        result.put("dpopBoundAccessTokens", client.isDpopBoundAccessTokens());
        if (client instanceof final OidcRegisteredService oidcClient) {
            val issuerService = oidcIssuerService.getIfAvailable();
            if (issuerService != null) {
                result.put("issuer", issuerService.determineIssuer(Optional.of(oidcClient)));
            }
            result.put("applicationType", oidcClient.getApplicationType());
            result.put("subjectType", oidcClient.getSubjectType());
            result.put("sectorIdentifierUri", oidcClient.getSectorIdentifierUri());
            result.put("signIdToken", oidcClient.isSignIdToken());
            result.put("idTokenSigningAlg", oidcClient.getIdTokenSigningAlg());
            result.put("encryptIdToken", oidcClient.isEncryptIdToken());
            result.put("idTokenEncryptionAlg", oidcClient.getIdTokenEncryptionAlg());
            result.put("jwks", describeClientJwks(oidcClient.getJwks()));
        }
        return result;
    }

    /**
     * List the keys of the CAS OpenID Connect keystore.
     *
     * @return the public key summaries
     */
    @McpTool(description = "List the keys of the CAS OpenID Connect keystore with their key id, type, algorithm, "
        + "use and rotation state (current, future or previous), along with the issuer. Key material is never returned.",
        title = "List OpenID Connect Keys",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> listJwksKeys() {
        val result = new LinkedHashMap<String, Object>();
        val issuerService = oidcIssuerService.getIfAvailable();
        if (issuerService != null) {
            result.put("issuer", issuerService.determineIssuer(Optional.empty()));
        }
        val generator = oidcJsonWebKeystoreGeneratorService.getIfAvailable();
        val resource = generator != null ? FunctionUtils.doUnchecked(() -> generator.find().orElse(null)) : null;
        if (resource == null) {
            result.put("count", 0);
            result.put("reason", "The OpenID Connect keystore is not available");
            return result;
        }
        val keys = FunctionUtils.doUnchecked(() -> OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource)).getJsonWebKeys()
            .stream()
            .map(CasOidcTools::describeKey)
            .toList();
        result.put("count", keys.size());
        result.put("keys", keys);
        return result;
    }

    private static Map<String, Object> describeKey(final JsonWebKey key) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("keyId", key.getKeyId());
        summary.put("keyType", key.getKeyType());
        summary.put("algorithm", key.getAlgorithm());
        summary.put("use", key.getUse());
        summary.put("state", OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.getJsonWebKeyState(key).name());
        return summary;
    }

    private static String describeClientJwks(final String jwks) {
        if (StringUtils.isBlank(jwks)) {
            return "NONE";
        }
        return Strings.CI.startsWithAny(jwks, "http://", "https://", "file:", "classpath:") ? jwks : "INLINE";
    }
}
