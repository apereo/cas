package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.RegisteredServiceOAuthResourceMatcher;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwk.JsonWebKey;
import org.jspecify.annotations.Nullable;
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
        + "redirect URI pattern, grant and response types, scopes, audience, allowed resource indicators (RFC 8707), "
        + "client authentication method, token settings and, "
        + "for OpenID Connect clients, the issuer and ID token signing and encryption settings. Client secrets are never "
        + "returned, only whether one is configured.",
        title = "Find OpenID Connect Client",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public OidcClient findOidcClient(
        @McpToolParam(description = "The client id of the OAuth or OpenID Connect client")
        final String clientId) {
        val client = findClient(clientId);
        if (client == null) {
            return OidcClient.builder()
                .clientId(clientId)
                .reason("No OAuth or OpenID Connect client is registered with client id " + clientId)
                .build();
        }
        val summary = OidcClient.builder()
            .clientId(clientId)
            .id(client.getId())
            .name(client.getName())
            .type(client.getFriendlyName())
            .redirectUriPattern(client.getServiceId())
            .enabled(client.getAccessStrategy().isServiceAccessAllowed(client, null))
            .supportedGrantTypes(sorted(client.getSupportedGrantTypes()))
            .supportedResponseTypes(sorted(client.getSupportedResponseTypes()))
            .scopes(sorted(client.getScopes()))
            .audience(sorted(client.getAudience()))
            .allowedResources(sorted(client.getAllowedResources()))
            .tokenEndpointAuthenticationMethod(client.getTokenEndpointAuthenticationMethod())
            .clientSecretConfigured(client.getClientSecrets() != null && !client.getClientSecrets().isEmpty())
            .bypassApprovalPrompt(client.isBypassApprovalPrompt())
            .generateRefreshToken(client.isGenerateRefreshToken())
            .jwtAccessToken(client.isJwtAccessToken())
            .dpopBoundAccessTokens(client.isDpopBoundAccessTokens())
            .tlsClientCertificateBoundAccessTokens(client.isTlsClientCertificateBoundAccessTokens());
        if (client instanceof final OidcRegisteredService oidcClient) {
            val issuerService = oidcIssuerService.getIfAvailable();
            if (issuerService != null) {
                summary.issuer(issuerService.determineIssuer(Optional.of(oidcClient)));
            }
            summary.applicationType(oidcClient.getApplicationType())
                .subjectType(oidcClient.getSubjectType())
                .sectorIdentifierUri(oidcClient.getSectorIdentifierUri())
                .signIdToken(oidcClient.isSignIdToken())
                .idTokenSigningAlg(oidcClient.getIdTokenSigningAlg())
                .encryptIdToken(oidcClient.isEncryptIdToken())
                .idTokenEncryptionAlg(oidcClient.getIdTokenEncryptionAlg())
                .jwks(describeClientJwks(oidcClient.getJwks()));
        }
        return summary.build();
    }

    /**
     * Check whether an OAuth or OpenID Connect client may request a resource indicator.
     *
     * @param clientId the client id
     * @param resource the resource indicator
     * @return the outcome
     */
    @McpTool(description = "Check whether an OAuth or OpenID Connect client may request a resource with the RFC 8707 "
        + "resource parameter, as CAS decides it: the resource must be an absolute URI without a fragment that fully matches "
        + "one of the regular expressions in the client's allowed resources. Reports the expression that matched, or why "
        + "the request would be refused with invalid_target, and the audience an access token issued for the resource carries.",
        title = "Check OAuth Client Resource",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public ResourceCheck checkOidcClientResource(
        @McpToolParam(description = "The client id of the OAuth or OpenID Connect client")
        final String clientId,
        @McpToolParam(description = "The resource indicator to check, i.e. https://api.example.org/orders")
        final String resource) {
        val client = findClient(clientId);
        if (client == null) {
            return ResourceCheck.builder().clientId(clientId).resource(resource).allowed(false)
                .reason("No OAuth or OpenID Connect client is registered with client id " + clientId)
                .build();
        }
        val check = ResourceCheck.builder()
            .clientId(clientId)
            .resource(resource)
            .allowedResources(sorted(client.getAllowedResources()));
        if (!RegisteredServiceOAuthResourceMatcher.isValidResource(resource)) {
            return check.allowed(false).reason("The resource is not an absolute URI without a fragment").build();
        }
        val matched = RegisteredServiceOAuthResourceMatcher.findAllowedResource(client.getAllowedResources(), resource);
        if (matched.isEmpty()) {
            return check.allowed(false).reason("The resource matches none of the allowed resources of the client").build();
        }
        val audience = new LinkedHashSet<String>(client.getAudience().isEmpty() ? List.of(client.getClientId()) : client.getAudience());
        audience.add(resource);
        return check.allowed(true).matchedPattern(matched.orElseThrow()).audience(List.copyOf(audience)).build();
    }

    /**
     * List the keys of the CAS OpenID Connect keystore.
     *
     * @return the public key summaries
     */
    @McpTool(description = "List the keys of the CAS OpenID Connect keystore with their key id, type, algorithm, "
        + "use and rotation state (current, future or previous), along with the issuer. Key material is never returned.",
        title = "List OpenID Connect Keys",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public JwksKeys listJwksKeys() {
        val issuerService = oidcIssuerService.getIfAvailable();
        val issuer = issuerService != null ? issuerService.determineIssuer(Optional.empty()) : null;
        val generator = oidcJsonWebKeystoreGeneratorService.getIfAvailable();
        val resource = generator != null ? FunctionUtils.doUnchecked(() -> generator.find().orElse(null)) : null;
        if (resource == null) {
            return new JwksKeys(issuer, 0, List.of(), "The OpenID Connect keystore is not available");
        }
        val keys = FunctionUtils.doUnchecked(() -> OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource)).getJsonWebKeys()
            .stream()
            .map(CasOidcTools::describeKey)
            .toList();
        return new JwksKeys(issuer, keys.size(), keys, null);
    }

    private static JwksKey describeKey(final JsonWebKey key) {
        return new JwksKey(key.getKeyId(), key.getKeyType(), key.getAlgorithm(), key.getUse(),
            OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.getJsonWebKeyState(key).name());
    }

    private @Nullable OAuthRegisteredService findClient(final String clientId) {
        return servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .filter(OAuthRegisteredService.class::isInstance)
            .map(OAuthRegisteredService.class::cast)
            .filter(service -> clientId.equals(service.getClientId()))
            .findFirst()
            .orElse(null);
    }

    private static String describeClientJwks(final String jwks) {
        if (StringUtils.isBlank(jwks)) {
            return "NONE";
        }
        return Strings.CI.startsWithAny(jwks, "http://", "https://", "file:", "classpath:") ? jwks : "INLINE";
    }

    private static List<String> sorted(final @Nullable Collection<String> values) {
        return values != null ? new TreeSet<>(values).stream().toList() : List.of();
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OidcClient(String clientId, @Nullable Long id, @Nullable String name, @Nullable String type,
                             @Nullable String redirectUriPattern, @Nullable Boolean enabled, @Nullable List<String> supportedGrantTypes,
                             @Nullable List<String> supportedResponseTypes, @Nullable List<String> scopes,
                             @Nullable List<String> audience, @Nullable List<String> allowedResources,
                             @Nullable String tokenEndpointAuthenticationMethod, @Nullable Boolean clientSecretConfigured,
                             @Nullable Boolean bypassApprovalPrompt, @Nullable Boolean generateRefreshToken,
                             @Nullable Boolean jwtAccessToken, @Nullable Boolean dpopBoundAccessTokens,
                             @Nullable Boolean tlsClientCertificateBoundAccessTokens, @Nullable String issuer,
                             @Nullable String applicationType, @Nullable String subjectType, @Nullable String sectorIdentifierUri,
                             @Nullable Boolean signIdToken, @Nullable String idTokenSigningAlg, @Nullable Boolean encryptIdToken,
                             @Nullable String idTokenEncryptionAlg, @Nullable String jwks, @Nullable String reason) {
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ResourceCheck(String clientId, String resource, boolean allowed, @Nullable String matchedPattern,
                                @Nullable List<String> allowedResources, @Nullable List<String> audience,
                                @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JwksKeys(@Nullable String issuer, int count, List<JwksKey> keys, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JwksKey(@Nullable String keyId, @Nullable String keyType, @Nullable String algorithm, @Nullable String use,
                          String state) {
    }
}
